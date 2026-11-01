package dev.daika.davy.feature.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimePlayer
import dev.daika.davy.core.domain.model.getPlayerByEpisodeId
import dev.daika.davy.core.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davyparsers.Parser
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

    private var anime: Anime? = null
    private var player: AnimePlayer? = null

    private var currentParser: Parser? = null
    private var fetchEpisodeJob: kotlinx.coroutines.Job? = null

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
        runCatching { currentParser?.closeSession() }
            .onFailure { Log.e("PlayerScreenViewModel", "Error closing parser", it) }
        currentParser = null
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
        _uiState.value = PlayerScreenUiState.Loading

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

            val interceptor = PlayerInterceptor()

            val parser = Parser.getParserForUrl(iframeUrl, parsers)
                ?: throw IllegalArgumentException("No parser found for $iframeUrl")

            currentParser = parser

            val playerData = parser.parse(
                iframeUrl,
                referer
            ) { interceptor.updateHeaders(it) }

            interceptor.setPlayerData(playerData)

            _uiState.value = PlayerScreenUiState.Success(
                availableResolutions = (playerData.translations.firstOrNull { it.isDefault }
                    ?: playerData.translations.first()).streams.sortedBy { it.quality.toInt() }
                    .map {
                        Resolution(
                            "https://davy/video/${it.quality}/${
                                it.urls.first().toHttpUrl().pathSegments.last()
                            }",
                            it.quality
                        )
                    },
                availableSubtitles = playerData.subtitles.map {
                    Subtitle(
                        it.label,
                        "https://davy/sub/${
                            URLEncoder.encode(it.label, "UTF-8").replace("+", "%20")
                        }/${it.src.toHttpUrl().pathSegments.last()}",
                        it.isDefault
                    )
                },
                dataSourceFactory = OkHttpDataSource.Factory(
                    okHttpClient.newBuilder().addInterceptor(interceptor).build()
                ),
                animeTitle = anime.title,
                episodeNumber = currentEpisode.title
            )
        } catch (e: CancellationException) {
            closeCurrentParser()
            throw e
        } catch (e: Exception) {
            closeCurrentParser()
            Log.e("PlayerScreenViewModel", "Error loading player data: ${e.message}", e)
            Log.e("PlayerScreenViewModel", e.stackTraceToString())
            _uiState.value = PlayerScreenUiState.Error(e.message ?: "Unknown error")
        }
    }
}

sealed interface PlayerScreenUiState {
    object Loading : PlayerScreenUiState
    data class Success(
        val availableResolutions: List<Resolution>,
        val availableSubtitles: List<Subtitle>,
        val dataSourceFactory: DataSource.Factory,
        val animeTitle: String,
        val episodeNumber: String
    ) : PlayerScreenUiState

    data class Error(val message: String) : PlayerScreenUiState
}

data class Resolution(
    val src: String,
    val quality: String
)

data class Subtitle(
    val label: String,
    val src: String,
    val default: Boolean
)