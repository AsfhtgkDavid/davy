package dev.daika.davy.ui.screens.anime

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeTranslation
import dev.daika.davy.ui.common.AnimeRow
import dev.daika.davy.ui.common.PosterImage
import dev.daika.davy.ui.common.RatingStars
import dev.daika.davy.ui.common.TvDropdown
import kotlinx.serialization.Serializable

@Composable
fun AnimeDetailsScreen(
    onEpisodeSelected: (Int, Int) -> Unit,
    onAnotherAnimeSelected: (Anime) -> Unit,
    animeDetailsScreenViewModel: AnimeDetailsScreenViewModel = hiltViewModel()
) {
    val state by animeDetailsScreenViewModel.uiState.collectAsState()
    var showPlayDialog by remember { mutableStateOf(false) }
    Log.i("AnimeDetailsScreen", "AnimeDetailsScreen called with state: $state")

    when (state) {
        is AnimeDetailsScreenUiState.Loading -> {
            // Show loading indicator
        }

        is AnimeDetailsScreenUiState.Success -> {
            val anime = (state as AnimeDetailsScreenUiState.Success).anime
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TopActionButtons(onPlayClicked = { showPlayDialog = true })

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PosterImage(
                        anime = anime, modifier = Modifier
                            .width(260.dp)
                            .aspectRatio(2f / 3f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    AnimeDescription(anime = anime, modifier = Modifier.weight(1f))
                }

                if (anime.viewingOrder.isNotEmpty()) {
                    ViewingOrderList(
                        viewingOrder = anime.viewingOrder,
                        onAnimeSelected = onAnotherAnimeSelected
                    )
                }
            }

            if (showPlayDialog) {
                PlaySelectionDialog(
                    animeTranslations = anime.translations,
                    onDismiss = { showPlayDialog = false },
                    onEpisodeSelected = { episodeId ->
                        showPlayDialog = false
                        onEpisodeSelected(anime.id, episodeId)
                    }
                )
            }
        }

        is AnimeDetailsScreenUiState.Error -> {
            // Show error message
        }
    }
}

@Composable
fun PlaySelectionDialog(
    animeTranslations: List<AnimeTranslation>,
    onDismiss: () -> Unit,
    onEpisodeSelected: (Int) -> Unit
) {
    var selectedTranslation by remember { mutableStateOf(animeTranslations.firstOrNull()) }
    var selectedPlayer by remember { mutableStateOf(selectedTranslation?.availablePlayers?.firstOrNull()) }

    var isTranslationExpanded by remember { mutableStateOf(false) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTranslation) {
        selectedPlayer = selectedTranslation?.availablePlayers?.firstOrNull()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(600.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    TvDropdown(
                        modifier = Modifier.weight(1f),
                        items = animeTranslations,
                        selectedItem = selectedTranslation,
                        onItemSelected = { selectedTranslation = it },
                        isExpanded = isTranslationExpanded,
                        onExpandedChange = {
                            isTranslationExpanded = it
                            if (it) isPlayerExpanded = false
                        },
                        itemText = { translation ->
                            val maxEps = translation.availablePlayers.maxOfOrNull {
                                it.episodes.size
                            } ?: 0
                            "${translation.title} ($maxEps eps)"
                        }
                    )

                    TvDropdown(
                        modifier = Modifier.weight(1f),
                        items = selectedTranslation?.availablePlayers ?: emptyList(),
                        selectedItem = selectedPlayer,
                        onItemSelected = { selectedPlayer = it },
                        isExpanded = isPlayerExpanded,
                        onExpandedChange = {
                            isPlayerExpanded = it
                            if (it) isTranslationExpanded = false
                        },
                        itemText = { player ->
                            "${player.player} (${player.episodes.size} eps)"
                        }
                    )
                }

                Text(
                    text = "Episodes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val episodes = selectedPlayer?.episodes ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(episodes) { episode ->
                        Button(
                            onClick = { onEpisodeSelected(episode.videoId) },
                            modifier = Modifier.aspectRatio(1f),
                            shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = episode.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopActionButtons(onPlayClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPlayClicked,
            scale = ButtonDefaults.scale(focusedScale = 1.05f),
            colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Play")
        }
    }
}

@Composable
private fun AnimeDescription(anime: Anime, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = anime.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (anime.otherTitles.isNotEmpty()) {
            Text(
                text = anime.otherTitles.joinToString(" / "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(anime.genres) { genre ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        RatingStars(rating = anime.rating.average, maxStars = 10)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = anime.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ViewingOrderList(viewingOrder: List<Anime>, onAnimeSelected: (Anime) -> Unit) {
    AnimeRow(
        modifier = Modifier.padding(top = 16.dp),
        animeList = viewingOrder,
        title = "Viewing Order",
        onAnimeSelected = onAnimeSelected
    )
}

@Serializable
data class AnimeDetailsScreenDestination(
    val animeId: Int
)