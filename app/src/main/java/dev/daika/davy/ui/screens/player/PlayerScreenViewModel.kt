package dev.daika.davy.ui.screens.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimePlayer
import dev.daika.davy.domain.entity.getPlayerByEpisodeId
import dev.daika.davy.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davyparsers.Parser
import dev.daika.davyparsers.PlayerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val yummyGetAnimeUseCase: YummyGetAnimeUseCase,
    private val parsers: List<@JvmSuppressWildcards Parser>
) : ViewModel() {
    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])
    private var episodeId: Int = checkNotNull(savedStateHandle["episodeId"])

    private var anime: Anime? = null
    private var player: AnimePlayer? = null

    private var _uiState = MutableStateFlow<PlayerScreenUiState>(PlayerScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fetchAnimeData()
            fetchEpisodeData()
        }
    }

    fun playNextEpisode() {
        val nextEpisode = player?.episodes?.firstOrNull { it.videoId > episodeId }
        if (nextEpisode != null) {
            episodeId = nextEpisode.videoId
            viewModelScope.launch {
                fetchEpisodeData()
            }
        } else {
            Log.d("PlayerScreenViewModel", "No next episode found.")
        }
    }

    fun playPreviousEpisode() {
        val previousEpisode = player?.episodes?.lastOrNull { it.videoId < episodeId }
        if (previousEpisode != null) {
            episodeId = previousEpisode.videoId
            viewModelScope.launch {
                fetchEpisodeData()
            }
        } else {
            Log.d("PlayerScreenViewModel", "No previous episode found.")
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
        try {
            val anime = anime ?: throw IllegalArgumentException("Anime not found")
            val iframeUrl = "https:${
                player?.episodes?.firstOrNull { it.videoId == episodeId }?.iframeUrl ?: throw IllegalArgumentException(
                    "Episode not found"
                )
            }"
            val referer = "https://old.yummyani.me/catalog/item/${anime.url}"
            Log.d(
                "PlayerScreenViewModel",
                "Fetching player data for iframeUrl: $iframeUrl with referer: $referer"
            )
            val playerData =
                Parser.getParserForUrl(iframeUrl, parsers)
                    ?.parse(
                        iframeUrl,
                        referer
                    )
                    ?: throw IllegalArgumentException("Failed to parse player data")
            _uiState.value = PlayerScreenUiState.Success(playerData, iframeUrl)
        } catch (e: Exception) {
            Log.e("PlayerScreenViewModel", "Error loading player data: ${e.message}", e)
            _uiState.value = PlayerScreenUiState.Error(e.message ?: "Unknown error")
        }
    }
}

sealed interface PlayerScreenUiState {
    object Loading : PlayerScreenUiState
    data class Success(val playerData: PlayerData, val iframeUrl: String) : PlayerScreenUiState
    data class Error(val message: String) : PlayerScreenUiState
}
