package dev.daika.davy.feature.details.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.ErrorView
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.core.ui.component.PosterImage
import dev.daika.davy.feature.details.AnimeDetailsScreenUiState
import dev.daika.davy.feature.details.AnimeDetailsScreenViewModel

@Composable
fun TvAnimeDetailsScreen(
    onEpisodeSelected: (Int, Int) -> Unit,
    onAnotherAnimeSelected: (Anime) -> Unit,
    animeDetailsScreenViewModel: AnimeDetailsScreenViewModel = hiltViewModel()
) {
    val state by animeDetailsScreenViewModel.uiState.collectAsState()
    var showPlayDialog by remember { mutableStateOf(false) }

    when (val currentState = state) {
        is AnimeDetailsScreenUiState.Loading -> {
            LoadingIndicator()
        }

        is AnimeDetailsScreenUiState.Success -> {
            val anime = currentState.anime
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showPlayDialog = true },
                        enabled = anime.hasPlayableEpisodes(),
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

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    PosterImage(
                        anime = anime,
                        modifier = Modifier
                            .width(260.dp)
                            .aspectRatio(2f / 3f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    TvAnimeDescription(anime = anime, modifier = Modifier.weight(1f))
                }

                if (anime.viewingOrder.isNotEmpty()) {
                    TvViewingOrderSection(
                        viewingOrder = anime.viewingOrder,
                        onAnimeSelected = onAnotherAnimeSelected
                    )
                }
            }

            if (showPlayDialog) {
                TvPlaySelectionDialog(
                    anime = anime,
                    onDismiss = { showPlayDialog = false },
                    onEpisodeSelected = { episodeId ->
                        showPlayDialog = false
                        onEpisodeSelected(anime.id, episodeId)
                    }
                )
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
