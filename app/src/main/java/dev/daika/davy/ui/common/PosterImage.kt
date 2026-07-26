package dev.daika.davy.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import dev.daika.davy.domain.entity.Anime

@Composable
fun PosterImage(
    anime: Anime,
    modifier: Modifier = Modifier
) {

    val posterUrl = when {
        anime.poster.startsWith("//") ->
            "https:${anime.poster}"

        anime.poster.startsWith("http") ->
            anime.poster

        else ->
            "https://${anime.poster}"
    }

    AsyncImage(
        modifier = modifier,
        model = posterUrl,
        contentDescription = "${anime.title} poster",
        contentScale = ContentScale.Crop
    )
}