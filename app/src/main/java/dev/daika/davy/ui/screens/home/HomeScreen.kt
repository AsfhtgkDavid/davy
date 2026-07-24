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
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeSeason
import dev.daika.davy.domain.entity.Feed
import dev.daika.davy.ui.common.AnimeRow
import kotlinx.serialization.Serializable

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
            SeasonList(
                feed = (state as HomeScreenUiState.Success).feed,
                onAnimeSelected = onAnimeSelected
            )
        }

        is HomeScreenUiState.Error -> {
        }
    }
}

@Composable
private fun SeasonList(
    feed: Feed,
    onAnimeSelected: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.i("HomeScreen", "SeasonList: ${feed.items.size} items")
    val lazyListState = rememberLazyListState()
    val animeSeason = AnimeSeason.fromSeasonNumber(feed.season)
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 108.dp),
        modifier = modifier
    ) {
        item(contentType = "SeasonRow") {
            AnimeRow(
                modifier = Modifier.padding(top = 16.dp),
                animeList = feed.items,
                title = "${animeSeason.title}'s 20${feed.year} Anime",
                onAnimeSelected = onAnimeSelected
            )
        }
    }
}

@Serializable
object HomeScreenDestination