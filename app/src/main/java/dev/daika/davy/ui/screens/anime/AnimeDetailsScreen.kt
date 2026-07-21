package dev.daika.davy.ui.screens.anime

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.domain.model.Anime
import dev.daika.davy.ui.common.PosterImage
import dev.daika.davy.ui.common.RatingStars
import kotlinx.serialization.Serializable

@Composable
fun AnimeDetailsScreen(
    onBackPressed: () -> Unit,
    animeDetailsViewModel: AnimeDetailsViewModel = hiltViewModel()
) {
    val state by animeDetailsViewModel.state.collectAsState()
    Log.i("AnimeDetailsScreen", "AnimeDetailsScreen called with state: $state")

    when (state) {
        is AnimeDetailsUiState.Loading -> {
            // Show loading indicator
        }

        is AnimeDetailsUiState.Success -> {
            val anime = (state as AnimeDetailsUiState.Success).anime
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TopActionButtons()

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
            }
        }

        is AnimeDetailsUiState.Error -> {
            // Show error message
        }
    }
}

@Composable
private fun TopActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {},
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
        Text(
            text = anime.otherTitles?.joinToString(" / ") ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(anime.genres ?: emptyList()) { genre ->
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
                        text = genre.title,
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

@Serializable
data class AnimeDetailsScreenDestination(
    val animeId: Int
)