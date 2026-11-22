package dev.daika.davy.feature.home.ui.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.ErrorView
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.core.ui.component.TvAnimeRow
import dev.daika.davy.core.ui.util.getDisplaySeasonTitle
import dev.daika.davy.feature.home.HomeScreenUiState
import dev.daika.davy.feature.home.HomeScreenViewModel

@Composable
fun TvHomeScreen(
    onAnimeSelected: (Anime) -> Unit,
    onSearchClicked: () -> Unit,
    onSwitchModeClicked: () -> Unit = {},
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by homeScreenViewModel.uiState.collectAsState()

    LazyColumn {
        item(contentType = "HeaderActions") {
            TvHomeHeader(
                onSearchClicked = onSearchClicked,
                onSwitchModeClicked = onSwitchModeClicked
            )
        }

        when (val currentState = state) {
            is HomeScreenUiState.Loading -> {
                item(contentType = "LoadingState") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
            }

            is HomeScreenUiState.Success -> {
                item(contentType = "AnimeRow") {
                    TvAnimeRow(
                        modifier = Modifier.padding(top = 8.dp),
                        animeList = currentState.feed.items,
                        title = currentState.feed.getDisplaySeasonTitle(),
                        onAnimeSelected = onAnimeSelected
                    )
                }
            }

            is HomeScreenUiState.Error -> {
                item(contentType = "ErrorState") {
                    ErrorView(
                        message = currentState.message,
                        onRetry = { homeScreenViewModel.loadFeed() }
                    )
                }
            }
        }
    }
}
