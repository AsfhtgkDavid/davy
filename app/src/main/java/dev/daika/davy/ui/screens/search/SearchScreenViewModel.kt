package dev.daika.davy.ui.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.usecase.YummySearchAnimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val yummySearchAnimeUseCase: YummySearchAnimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchScreenUiState>(
        SearchScreenUiState.Idle
    )

    val uiState = _uiState.asStateFlow()


    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchScreenUiState.Idle
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = SearchScreenUiState.Loading

                val results = yummySearchAnimeUseCase(query)

                _uiState.value = SearchScreenUiState.Success(results)

            } catch (e: Exception) {
                _uiState.value =
                    SearchScreenUiState.Error(
                        e.message ?: "Unknown error"
                    )

                Log.e(
                    "SearchScreenViewModel",
                    "Error searching anime: ${e.message}",
                    e
                )
            }
        }
    }
}


sealed interface SearchScreenUiState {
    data object Idle : SearchScreenUiState

    data object Loading : SearchScreenUiState

    data class Success(
        val anime: List<Anime>
    ) : SearchScreenUiState

    data class Error(
        val message: String
    ) : SearchScreenUiState
}