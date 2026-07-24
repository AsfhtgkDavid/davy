package dev.daika.davy.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import dev.daika.davy.domain.entity.Anime

@Composable
fun PosterImage(anime: Anime, modifier: Modifier = Modifier) {
    AsyncImage(
        modifier = modifier,
        model = "https://${anime.poster}",
        contentDescription = "${anime.title} poster",
        contentScale = ContentScale.Crop
    )
}