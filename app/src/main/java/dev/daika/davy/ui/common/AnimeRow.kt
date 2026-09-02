package dev.daika.davy.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.daika.davy.domain.entity.Anime

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
                    .padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
            )
        }
        AnimatedContent(
            targetState = animeList,
            label = ""
        ) { animeState ->
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(
                    animeState,
                    key = { _, anime -> anime.id },
                    contentType = { _, _ -> "AnimeItem" }) { index, anime ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    AnimeItem(
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
