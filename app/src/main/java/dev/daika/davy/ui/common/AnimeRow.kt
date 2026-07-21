package dev.daika.davy.ui.common

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.daika.davy.domain.model.Anime

@Composable
fun AnimeRow(
    animeList: List<Anime>,
    modifier: Modifier = Modifier,
    title: String? = null,
    onAnimeSelected: (Anime) -> Unit = {}
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

    Column(
        modifier = modifier.focusGroup()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 30.sp
                ),
                modifier = Modifier
                    .alpha(1f)
                    .padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
            )
        }
        AnimatedContent(
            targetState = animeList,
            label = ""
        ) { animeState ->
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(animeState, key = { _, anime -> anime.id }) { index, anime ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    AnimeRowItem(
                        anime = anime,
                        modifier = itemModifier,
                        index = index,
                        onAnimeSelected = onAnimeSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeRowItem(
    anime: Anime,
    modifier: Modifier,
    index: Int,
    onAnimeSelected: (Anime) -> Unit,
    onAnimeFocused: (Anime) -> Unit = {}
) {
    Log.i("AnimeRowItem", "AnimeRowItem called for anime: ${anime.title}")
    var isFocused by remember { mutableStateOf(false) }
    AnimeCard(
        title = {
            AnimeRowItemTitle(
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
        AnimeRowItemImage(
            anime = anime,
        )
    }
}

@Composable
private fun AnimeRowItemTitle(anime: Anime, isFocused: Boolean) {
    val movieNameAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        label = "",
    )
    Text(
        text = anime.title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .alpha(movieNameAlpha)
            .fillMaxWidth()
            .padding(top = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AnimeRowItemImage(anime: Anime, modifier: Modifier = Modifier) {
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

