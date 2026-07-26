package dev.daika.davy.ui.common

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.daika.davy.domain.entity.Anime


@Composable
fun AnimePosterCard(
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimeCard(
        onClick = onClick,
        modifier = modifier.width(180.dp),
        title = {
            Text(
                text = anime.title,
                maxLines = 1
            )
        },
        image = {
            PosterImage(
                anime = anime
            )
        }
    )
}