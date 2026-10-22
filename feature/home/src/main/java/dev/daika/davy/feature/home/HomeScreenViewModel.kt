package dev.daika.davy.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.daika.davy.core.domain.model.Feed
import dev.daika.davy.core.domain.usecase.YummyGetFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
object HomeScreenDestination

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val yummyGetFeedUseCase: YummyGetFeedUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        _uiState.value = HomeScreenUiState.Loading
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
