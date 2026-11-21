package dev.daika.davy.feature.home.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.ErrorView
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.feature.home.HomeScreenUiState
import dev.daika.davy.feature.home.HomeScreenViewModel

@Composable
fun MobileHomeScreen(
    onAnimeSelected: (Anime) -> Unit,
    onSearchClicked: () -> Unit,
    onSwitchModeClicked: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by homeScreenViewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            MobileHomeTopBar(
                onSearchClicked = onSearchClicked,
                onSwitchModeClicked = onSwitchModeClicked
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (val currentState = state) {
                    is HomeScreenUiState.Loading -> {
                        LoadingIndicator()
                    }

                    is HomeScreenUiState.Success -> {
                        MobileFeedGrid(
                            feed = currentState.feed,
                            onAnimeSelected = onAnimeSelected
                        )
                    }

                    is HomeScreenUiState.Error -> {
                        ErrorView(
                            title = "Failed to load data",
                            message = currentState.message,
                            onRetry = { homeScreenViewModel.loadFeed() }
                        )
                    }
                }
            }
        }
    }
}
