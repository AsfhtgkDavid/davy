package dev.daika.davy.ui.screens.anime

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.data.repository.YummyRepository
import dev.daika.davy.domain.model.Anime
import dev.daika.davy.domain.usecase.YummyGetAnimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAnimeUseCase: YummyGetAnimeUseCase
) : ViewModel() {
    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private var _state = MutableStateFlow<AnimeDetailsUiState>(AnimeDetailsUiState.Loading)
    val state = _state.asStateFlow()

    init {
        getAnimeDetails()
    }

    private fun getAnimeDetails() {
        viewModelScope.launch {
            try {
                val animeDetails = getAnimeUseCase(animeId)
                _state.value = AnimeDetailsUiState.Success(animeDetails)
            } catch (e: Exception) {
                _state.value = AnimeDetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface AnimeDetailsUiState {
    object Loading : AnimeDetailsUiState
    data class Success(val anime: Anime) : AnimeDetailsUiState
    data class Error(val message: String) : AnimeDetailsUiState
}
