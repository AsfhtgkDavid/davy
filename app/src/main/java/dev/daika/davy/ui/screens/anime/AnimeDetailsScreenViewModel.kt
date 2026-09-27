package dev.daika.davy.ui.screens.anime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.filterSupportedPlayers
import dev.daika.davy.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davyparsers.Parser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAnimeUseCase: YummyGetAnimeUseCase,
    private val parsers: List<@JvmSuppressWildcards Parser>
) : ViewModel() {
    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private var _uiState =
        MutableStateFlow<AnimeDetailsScreenUiState>(AnimeDetailsScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getAnimeDetails()
    }

    private fun getAnimeDetails() {
        viewModelScope.launch {
            try {
                val animeDetails = getAnimeUseCase(animeId, true)
                val filteredAnime = animeDetails.copy(
                    translations = animeDetails.translations.filterSupportedPlayers(parsers)
                )
                _uiState.value = AnimeDetailsScreenUiState.Success(filteredAnime)
            } catch (e: Exception) {
                _uiState.value = AnimeDetailsScreenUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface AnimeDetailsScreenUiState {
    object Loading : AnimeDetailsScreenUiState
    data class Success(val anime: Anime) : AnimeDetailsScreenUiState
    data class Error(val message: String) : AnimeDetailsScreenUiState
}
