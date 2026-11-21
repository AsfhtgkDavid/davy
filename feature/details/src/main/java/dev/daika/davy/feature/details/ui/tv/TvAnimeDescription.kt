package dev.daika.davy.feature.details.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.AnimeGenreChip
import dev.daika.davy.core.ui.component.AnimeStatusBadge
import dev.daika.davy.core.ui.component.RatingStars

@Composable
fun TvAnimeDescription(
    anime: Anime,
    modifier: Modifier = Modifier
) {
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

        AnimeStatusBadge(
            status = anime.status,
            shape = RoundedCornerShape(50),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (anime.genres.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(anime.genres) { genre ->
                    AnimeGenreChip(
                        genre = genre,
                        backgroundTransparent = true,
                        modifier = Modifier.padding(vertical = 4.dp)
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
