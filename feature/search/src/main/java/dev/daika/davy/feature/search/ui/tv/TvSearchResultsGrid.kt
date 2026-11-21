package dev.daika.davy.feature.search.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.TvAnimeItem

@Composable
fun TvSearchResultsGrid(
    lazyPagingItems: LazyPagingItems<Anime>,
    onAnimeSelected: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(lazyPagingItems.itemCount) { index ->
            val anime = lazyPagingItems[index]
            if (anime != null) {
                TvAnimeItem(
                    index = index,
                    anime = anime,
                    onAnimeSelected = onAnimeSelected,
                    modifier = Modifier.focusProperties {
                        left = if (index % 6 == 0) FocusRequester.Cancel else FocusRequester.Default
                    }
                )
            }
        }

        if (lazyPagingItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
