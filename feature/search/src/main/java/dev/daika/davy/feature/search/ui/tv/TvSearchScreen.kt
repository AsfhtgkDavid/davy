package dev.daika.davy.feature.search.ui.tv

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.feature.search.SearchScreenViewModel

@Composable
fun TvSearchScreen(
    onAnimeSelected: (Anime) -> Unit = {},
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel()
) {
    val lazyPagingItems = searchScreenViewModel.pagedItems.collectAsLazyPagingItems()
    val genreOptions by searchScreenViewModel.genreOptions.collectAsStateWithLifecycle()
    val searchQuery by searchScreenViewModel.searchQuery.collectAsStateWithLifecycle()
    val filterState by searchScreenViewModel.filterState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TvSearchTopBar(
            searchQuery = searchQuery,
            filterState = filterState,
            genreOptions = genreOptions,
            onQueryChange = { searchScreenViewModel.updateSearchQuery(it) },
            onFilterStateChange = { searchScreenViewModel.updateFilterState(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TvSearchResultsGrid(
            lazyPagingItems = lazyPagingItems,
            onAnimeSelected = onAnimeSelected
        )
    }
}
