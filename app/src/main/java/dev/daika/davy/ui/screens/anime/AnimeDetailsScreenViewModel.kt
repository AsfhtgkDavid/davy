package dev.daika.davy.ui.screens.anime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.usecase.YummyGetAnimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAnimeUseCase: YummyGetAnimeUseCase
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
                _uiState.value = AnimeDetailsScreenUiState.Success(animeDetails)
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
