package dev.daika.davy.feature.details.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.AnimeGenreChip
import dev.daika.davy.core.ui.component.ErrorView
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.feature.details.AnimeDetailsScreenUiState
import dev.daika.davy.feature.details.AnimeDetailsScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileAnimeDetailsScreen(
    onBackPressed: () -> Unit,
    onEpisodeSelected: (Int, Int) -> Unit,
    onAnotherAnimeSelected: (Anime) -> Unit,
    animeDetailsScreenViewModel: AnimeDetailsScreenViewModel = hiltViewModel()
) {
    val state by animeDetailsScreenViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state is AnimeDetailsScreenUiState.Success) {
                            (state as AnimeDetailsScreenUiState.Success).anime.title
                        } else {
                            ""
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val currentState = state) {
                is AnimeDetailsScreenUiState.Loading -> {
                    LoadingIndicator()
                }

                is AnimeDetailsScreenUiState.Success -> {
                    val anime = currentState.anime

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        MobileAnimeHeader(anime = anime)

                        Spacer(modifier = Modifier.height(16.dp))

                        val hasPlayable = anime.hasPlayableEpisodes()
                        Button(
                            onClick = {
                                // TODO: Sync with history
                                val firstEpisode = anime.translations
                                    .firstOrNull()?.availablePlayers?.firstOrNull()?.episodes?.firstOrNull()
                                if (firstEpisode != null) {
                                    onEpisodeSelected(anime.id, firstEpisode.videoId)
                                }
                            },
                            enabled = hasPlayable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch online",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        if (anime.genres.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(anime.genres) { genre ->
                                    AnimeGenreChip(genre = genre, backgroundTransparent = false)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        MobileAnimeDescription(description = anime.description)

                        Spacer(modifier = Modifier.height(20.dp))

                        MobileEpisodeSelector(
                            translations = anime.translations,
                            onEpisodeSelected = { videoId ->
                                onEpisodeSelected(anime.id, videoId)
                            }
                        )

                        if (anime.viewingOrder.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            MobileViewingOrderSection(
                                viewingOrder = anime.viewingOrder,
                                onAnimeSelected = onAnotherAnimeSelected
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                is AnimeDetailsScreenUiState.Error -> {
                    ErrorView(
                        message = currentState.message,
                        onRetry = { animeDetailsScreenViewModel.getAnimeDetails() }
                    )
                }
            }
        }
    }
}
