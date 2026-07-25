package dev.daika.davy.ui.common

import android.util.Log
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import dev.daika.davy.domain.entity.Anime

@Composable
fun AnimeItem(
    anime: Anime,
    modifier: Modifier,
    index: Int,
    onAnimeSelected: (Anime) -> Unit,
    onAnimeFocused: (Anime) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    AnimeCard(
        title = {
            AnimeItemTitle(
                anime = anime,
                isFocused = isFocused,
            )
        },
        modifier = Modifier
            .width(150.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (isFocused) {
                    onAnimeFocused(anime)
                }
            }
            .focusProperties {
                left = if (index == 0) FocusRequester.Cancel else FocusRequester.Default
            }
            .then(modifier),
        onClick = { onAnimeSelected(anime) }
    ) {
        AnimeItemImage(
            anime = anime,
        )
    }
}

@Composable
private fun AnimeItemTitle(anime: Anime, isFocused: Boolean) {
    val modifier = if (isFocused) {
        Modifier.basicMarquee()
    } else {
        Modifier.Companion
    }
    Text(
        text = anime.title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .then(modifier),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AnimeItemImage(anime: Anime, modifier: Modifier = Modifier.Companion) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        PosterImage(
            anime = anime,
            modifier = Modifier
                .fillMaxSize()
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", anime.rating.average),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}