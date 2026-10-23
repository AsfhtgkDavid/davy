package dev.daika.davy.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import dev.daika.davy.core.domain.model.Anime

@Composable
fun PosterImage(anime: Anime, modifier: Modifier = Modifier) {
    AsyncImage(
        modifier = modifier,
        model = "https://${anime.poster}",
        contentDescription = "${anime.title} poster",
        contentScale = ContentScale.Crop
    )
}
