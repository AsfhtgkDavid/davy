package dev.daika.davy.ui.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.usecase.YummySearchAnime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val yummySearchAnime: YummySearchAnime
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchScreenUiState>(SearchScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun searchAnime(query: String) {
        viewModelScope.launch {
            try {
                val animeList = yummySearchAnime(query)
                _uiState.value = SearchScreenUiState.Success(animeList)
            } catch (e: Exception) {
                _uiState.value = SearchScreenUiState.Error(e.message ?: "Unknown error")
                Log.e("SearchScreenViewModel", "Error searching anime", e)
            }
        }
    }
}

sealed interface SearchScreenUiState {
    object Loading : SearchScreenUiState
    data class Success(val animeList: List<Anime>) : SearchScreenUiState
    data class Error(val message: String) : SearchScreenUiState
}
