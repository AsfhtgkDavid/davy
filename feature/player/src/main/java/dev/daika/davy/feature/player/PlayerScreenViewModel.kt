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
import dev.daika.davyparsers.Parser
import dev.daika.davyparsers.ParserEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
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
    private val okHttpClient: OkHttpClient
) : ViewModel() {
    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])
    private var episodeId: Int = checkNotNull(savedStateHandle["episodeId"])

    private val interceptor = PlayerInterceptor()
    val dataSourceFactory = OkHttpDataSource.Factory(
        okHttpClient.newBuilder().addInterceptor(interceptor).build()
    )

    private var anime: Anime? = null
    private var player: AnimePlayer? = null

    private var currentParser: Parser? = null
    private var fetchEpisodeJob: kotlinx.coroutines.Job? = null
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
        val nextEpisode = player?.episodes?.firstOrNull { it.videoId > episodeId }
        if (nextEpisode != null) {
            episodeId = nextEpisode.videoId
            reloadEpisode()
        } else {
            Log.d("PlayerScreenViewModel", "No next episode found.")
        }
    }

    fun playPreviousEpisode() {
        val previousEpisode = player?.episodes?.lastOrNull { it.videoId < episodeId }
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
        } catch (e: Exception) {
            Log.e("PlayerScreenViewModel", "Error fetching anime data: ${e.message}", e)
        }
    }

    private suspend fun fetchEpisodeData() {
        val previousSuccess = _uiState.value as? PlayerScreenUiState.Success
        if (previousSuccess != null) {
            _uiState.value = previousSuccess.copy(isEpisodeLoading = true)
        } else {
            _uiState.value = PlayerScreenUiState.Loading
        }

        closeCurrentParser()

        try {
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

            interceptor.setPlayerData(playerData)

            val translation = (playerData.translations.firstOrNull { it.isDefault }
                ?: playerData.translations.first())
            audioId = translation.id

            _uiState.value = PlayerScreenUiState.Success(
                availableResolutions = translation.streams.sortedBy { it.quality.toInt() }
                    .map {
                        Resolution(
                            "https://davy/video/${it.quality}/${
                                it.urls.first().toHttpUrl().pathSegments.last()
                            }",
                            it.quality
                        )
                    },
                availableSubtitles = playerData.subtitles.mapIndexed { index, track ->
                    Subtitle(
                        index,
                        track.label,
                        "https://davy/sub/${
                            URLEncoder.encode(track.label, "UTF-8").replace("+", "%20")
                        }/${track.src.toHttpUrl().pathSegments.last()}",
                        track.isDefault
                    )
                },
                animeTitle = anime.title,
                episodeNumber = currentEpisode.title,
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