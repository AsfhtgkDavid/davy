package dev.daika.davy.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color

@Composable
fun HomeScreen(
    onAnimeSelected: (Anime) -> Unit,
    onSearchClicked: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    Log.i("HomeScreen", "HomeScreen called")
    val state by homeScreenViewModel.uiState.collectAsState()

    when (state) {
        is HomeScreenUiState.Loading -> {
            Text("Loading...")
        }

        is HomeScreenUiState.Success -> {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            onSearchClicked()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            tint = Color.White,
                            contentDescription = "Search anime"
                        )
                    }
                }

                SeasonList(
                    topCarousel = (state as HomeScreenUiState.Success).feed.topCarousel,
                    onAnimeSelected = onAnimeSelected
                )
            }
            SeasonList(
                feed = (state as HomeScreenUiState.Success).feed,
                onAnimeSelected = onAnimeSelected
            )
        }

        is HomeScreenUiState.Error -> {
            Text("Error loading anime")
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