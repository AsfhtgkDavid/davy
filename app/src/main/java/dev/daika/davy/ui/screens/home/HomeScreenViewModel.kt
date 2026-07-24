package dev.daika.davy.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.data.model.YummyFeed
import dev.daika.davy.domain.entity.Feed
import dev.daika.davy.domain.usecase.YummyGetFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val yummyGetFeedUseCase: YummyGetFeedUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            try {
                val feed = yummyGetFeedUseCase()
                _uiState.value = HomeScreenUiState.Success(feed)
            } catch (e: Exception) {
                _uiState.value = HomeScreenUiState.Error(e.message ?: "Unknown error")
                Log.e("HomeScreenViewModel", "Error loading feed: ${e.message}", e)
            }
        }
    }
}

sealed interface HomeScreenUiState {
    object Loading : HomeScreenUiState
    data class Success(val feed: Feed) : HomeScreenUiState
    data class Error(val message: String) : HomeScreenUiState
}