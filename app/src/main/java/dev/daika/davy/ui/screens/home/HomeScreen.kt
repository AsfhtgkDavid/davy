package dev.daika.davy.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeSeason
import dev.daika.davy.domain.entity.Feed
import dev.daika.davy.ui.common.AnimeRow
import kotlinx.serialization.Serializable

@Composable
fun HomeScreen(
    onAnimeSelected: (Anime) -> Unit,
    onSearchClicked: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    Log.i("HomeScreen", "HomeScreen called")
    val state by homeScreenViewModel.uiState.collectAsState()
    LazyColumn {
        item(contentType = "SearchButton") {
            IconButton(
                onClick = onSearchClicked,
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp),
                shape = ButtonDefaults.shape(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            }
        }

        when (state) {
            is HomeScreenUiState.Loading -> {
            }

            is HomeScreenUiState.Success -> {
                item(contentType = "AnimeRow") {
                    SeasonList(
                        feed = (state as HomeScreenUiState.Success).feed,
                        onAnimeSelected = onAnimeSelected
                    )
                }
            }

            is HomeScreenUiState.Error -> {
            }
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
    val animeSeason = AnimeSeason.fromSeasonNumber(feed.season)
    AnimeRow(
        modifier = modifier.padding(top = 8.dp),
        animeList = feed.items,
        title = "${animeSeason.title}'s 20${feed.year} Anime",
        onAnimeSelected = onAnimeSelected
    )
}

@Serializable
object HomeScreenDestination