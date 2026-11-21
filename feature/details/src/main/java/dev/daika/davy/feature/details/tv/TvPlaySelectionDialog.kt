package dev.daika.davy.feature.details.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.TvDropdown

@Composable
fun TvPlaySelectionDialog(
    anime: Anime,
    onDismiss: () -> Unit,
    onEpisodeSelected: (Int) -> Unit
) {
    val isSingleEpisodeTitle = anime.isSingleEpisodeTitle()

    var selectedTranslation by remember { mutableStateOf(anime.translations.firstOrNull()) }
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
                if (isSingleEpisodeTitle) {
                    TvDropdown(
                        modifier = Modifier.fillMaxWidth(),
                        items = anime.translations,
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
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        TvDropdown(
                            modifier = Modifier.weight(1f),
                            items = anime.translations,
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
                }

                Text(
                    text = if (isSingleEpisodeTitle) "Players" else "Episodes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isSingleEpisodeTitle) {
                    val players = selectedTranslation?.availablePlayers ?: emptyList()
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(players) { player ->
                            Button(
                                onClick = {
                                    player.episodes.firstOrNull()
                                        ?.let { onEpisodeSelected(it.videoId) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                                )
                            ) {
                                Text(
                                    text = player.player,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
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
}
