package dev.daika.davy.feature.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimePlayer
import dev.daika.davy.core.domain.model.getPlayerByEpisodeId
import dev.daika.davy.core.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davy.feature.player.network.PlayerInterceptor
import dev.daika.davyparsers.Parser
import dev.daika.davyparsers.ParserEvent
import dev.daika.davyparsers.PlayerData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject

@Serializable
data class PlayerScreenDestination(
    val animeId: Int,
    val episodeId: Int
)

@HiltViewModel
class PlayerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val yummyGetAnimeUseCase: YummyGetAnimeUseCase,
    private val parsers: List<@JvmSuppressWildcards Parser>,
    okHttpClient: OkHttpClient
) : ViewModel() {
    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])
    private var episodeId: Int = checkNotNull(savedStateHandle["episodeId"])

    private val interceptor = PlayerInterceptor {
        runBlocking {
            try {
                parseCurrentEpisode().first
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw IOException("Failed to refresh player data", e)
            }
        }
    }

    val dataSourceFactory = OkHttpDataSource.Factory(
        okHttpClient.newBuilder().addInterceptor(interceptor).build()
    )

    private var anime: Anime? = null
    private var player: AnimePlayer? = null

    private var currentParser: Parser? = null
    private var fetchEpisodeJob: Job? = null
    private var audioId: String = "0"

    private var _uiState = MutableStateFlow<PlayerScreenUiState>(PlayerScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        addCloseable {
            closeCurrentParser()
        }

        fetchEpisodeJob = viewModelScope.launch {
            fetchAnimeData()
            fetchEpisodeData()
        }
    }

    private fun closeCurrentParser() {
        currentParser?.closeSession()
        currentParser = null
    }

    private fun sendParserEvent(event: ParserEvent) {
        currentParser?.sendEvent(event)
    }

    fun onResumed(currentTime: Long) {
        sendParserEvent(ParserEvent.RESUMED(currentTime))
    }

    fun onPaused(currentTime: Long) {
        sendParserEvent(ParserEvent.PAUSED(currentTime))
    }

    fun onSeeked(currentTime: Long) {
        sendParserEvent(ParserEvent.SEEKED(currentTime))
    }

    fun onTrackChanged(currentTime: Long, resolution: String, subtitleId: Int) {
        sendParserEvent(
            ParserEvent.TRACK_CHANGED(
                currentTime = currentTime,
                resolution = resolution,
                audioId = audioId,
                subtitleId = subtitleId
            )
        )
    }

    fun playNextEpisode() {
        val episodes = player?.episodes ?: return
        val currentIndex = episodes.indexOfFirst { it.videoId == episodeId }
        val nextEpisode = if (currentIndex != -1) episodes.getOrNull(currentIndex + 1) else null
        if (nextEpisode != null) {
            episodeId = nextEpisode.videoId
            reloadEpisode()
        } else {
            Log.d("PlayerScreenViewModel", "No next episode found.")
        }
    }

    fun playPreviousEpisode() {
        val episodes = player?.episodes ?: return
        val currentIndex = episodes.indexOfFirst { it.videoId == episodeId }
        val previousEpisode = if (currentIndex > 0) episodes.getOrNull(currentIndex - 1) else null
        if (previousEpisode != null) {
            episodeId = previousEpisode.videoId
            reloadEpisode()
        } else {
            Log.d("PlayerScreenViewModel", "No previous episode found.")
        }
    }

    private fun reloadEpisode() {
        fetchEpisodeJob?.cancel()
        fetchEpisodeJob = viewModelScope.launch {
            fetchEpisodeData()
        }
    }

    private suspend fun fetchAnimeData() {
        try {
            anime = yummyGetAnimeUseCase(animeId, true)
            player = anime?.translations?.getPlayerByEpisodeId(episodeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("PlayerScreenViewModel", "Error fetching anime data: ${e.message}", e)
        }
    }

    private suspend fun parseCurrentEpisode(): Pair<PlayerData, String> {
        closeCurrentParser()

        val anime = anime ?: throw IllegalArgumentException("Anime not found")
        val currentEpisode = player?.episodes?.firstOrNull { it.videoId == episodeId }
            ?: throw IllegalArgumentException("Episode not found")

        val iframeUrl = if (currentEpisode.iframeUrl.startsWith("http"))
            currentEpisode.iframeUrl
        else
            "https:${currentEpisode.iframeUrl}"
        val referer = "https://old.yummyani.me/catalog/item/${anime.url}"

        val parser = Parser.getParserForUrl(iframeUrl, parsers)
            ?: throw IllegalArgumentException("No parser found for $iframeUrl")

        currentParser = parser

        val playerData = parser.parse(
            iframeUrl,
            referer
        ) { interceptor.updateHeaders(it) }

        return playerData to currentEpisode.title
    }

    private suspend fun fetchEpisodeData() {
        val previousSuccess = _uiState.value as? PlayerScreenUiState.Success
        if (previousSuccess != null) {
            _uiState.value = previousSuccess.copy(isEpisodeLoading = true)
        } else {
            _uiState.value = PlayerScreenUiState.Loading
        }

        try {
            val anime = anime ?: throw IllegalArgumentException("Anime not found")
            val (playerData, episodeTitle) = parseCurrentEpisode()

            interceptor.setPlayerData(playerData)
            val translation = playerData.translations.firstOrNull { it.isDefault }
                ?: playerData.translations.firstOrNull()
                ?: throw IllegalStateException("No video translations available")
            audioId = translation.id

            val sortedResolutions = translation.streams
                .sortedBy { stream ->
                    stream.quality.filter(Char::isDigit).toIntOrNull() ?: 0
                }
                .map {
                    Resolution(
                        src = "https://davy/video/${it.quality}/${
                            it.urls.first().toHttpUrl().pathSegments.last()
                        }",
                        quality = it.quality
                    )
                }

            if (sortedResolutions.isEmpty()) {
                throw IllegalStateException("No video streams available")
            }

            _uiState.value = PlayerScreenUiState.Success(
                availableResolutions = sortedResolutions,
                availableSubtitles = playerData.subtitles.mapIndexed { index, track ->
                    Subtitle(
                        id = index,
                        label = track.label,
                        src = "https://davy/sub/${
                            URLEncoder.encode(track.label, "UTF-8").replace("+", "%20")
                        }/${track.src.toHttpUrl().pathSegments.last()}",
                        default = track.isDefault
                    )
                },
                animeTitle = anime.title,
                episodeNumber = episodeTitle,
                isEpisodeLoading = false
            )
        } catch (e: CancellationException) {
            closeCurrentParser()
            throw e
        } catch (e: Exception) {
            closeCurrentParser()
            Log.e("PlayerScreenViewModel", "Error loading player data: ${e.message}", e)
            _uiState.value = PlayerScreenUiState.Error(e.message ?: "Unknown error")
        }
    }
}

sealed interface PlayerScreenUiState {
    object Loading : PlayerScreenUiState
    data class Success(
        val availableResolutions: List<Resolution>,
        val availableSubtitles: List<Subtitle>,
        val animeTitle: String,
        val episodeNumber: String,
        val isEpisodeLoading: Boolean = false
    ) : PlayerScreenUiState

    data class Error(val message: String) : PlayerScreenUiState
}

data class Resolution(
    val src: String,
    val quality: String
)

data class Subtitle(
    val id: Int,
    val label: String,
    val src: String,
    val default: Boolean
)