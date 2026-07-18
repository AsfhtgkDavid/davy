package dev.daika.davy.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.daika.davy.domain.model.Anime
import dev.daika.davy.ui.common.AnimeRow

@Composable
fun HomeScreen(
    onAnimeSelected: (Anime) -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    Log.i("HomeScreen", "HomeScreen called")
    val state by homeScreenViewModel.uiState.collectAsState()

    when (state) {
        is HomeScreenUiState.Loading -> {
        }

        is HomeScreenUiState.Success -> {
            AnnouncementsList(
                announcements = (state as HomeScreenUiState.Success).feed.announcements,
                onAnimeSelected = onAnimeSelected
            )
        }

        is HomeScreenUiState.Error -> {
        }
    }
}

@Composable
fun AnnouncementsList(
    announcements: List<Anime>,
    onAnimeSelected: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.i("HomeScreen", "AnnouncementsList: ${announcements.size} announcements")
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 108.dp),
        modifier = modifier
    ) {
        item(contentType = "AnnouncementRow") {
            AnimeRow(
                modifier = Modifier.padding(top = 16.dp),
                animeList = announcements,
                title = "Announcements",
                onAnimeSelected = onAnimeSelected
            )
        }
    }
}

