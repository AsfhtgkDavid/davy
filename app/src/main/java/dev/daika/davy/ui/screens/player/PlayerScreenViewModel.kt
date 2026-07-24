package dev.daika.davy.ui.screens.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.domain.entity.getIframeById
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
    private val episodeId: Int = checkNotNull(savedStateHandle["episodeId"])

    private var _uiState = MutableStateFlow<PlayerScreenUiState>(PlayerScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getPlayerData()
    }

    fun getPlayerData() {
        _uiState.value = PlayerScreenUiState.Loading
        viewModelScope.launch {
            try {
                val anime = yummyGetAnimeUseCase(animeId, true)
                val iframeUrl = "https:${
                    anime.translations.getIframeById(episodeId) ?: throw IllegalArgumentException("Episode not found")
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
}

sealed interface PlayerScreenUiState {
    object Loading : PlayerScreenUiState
    data class Success(val playerData: PlayerData, val iframeUrl: String) : PlayerScreenUiState
    data class Error(val message: String) : PlayerScreenUiState
}
