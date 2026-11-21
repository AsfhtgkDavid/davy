package dev.daika.davy.feature.search.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.feature.search.SearchScreenViewModel

@Composable
fun MobileSearchScreen(
    onBackPressed: () -> Unit,
    onAnimeSelected: (Anime) -> Unit,
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel()
) {
    val lazyPagingItems = searchScreenViewModel.pagedItems.collectAsLazyPagingItems()
    val genreOptions by searchScreenViewModel.genreOptions.collectAsStateWithLifecycle()
    val searchQuery by searchScreenViewModel.searchQuery.collectAsStateWithLifecycle()
    val filterState by searchScreenViewModel.filterState.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }

    val activeFiltersCount = remember(filterState) {
        var count = 0
        count += filterState.selectedGenres.size
        count += filterState.selectedStatuses.size
        count += filterState.selectedTypes.size
        if (filterState.yearFrom != null || filterState.yearTo != null) count++
        if (filterState.selectedSort != null && filterState.selectedSort != "none") count++
        count
    }

    Scaffold(
        topBar = {
            MobileSearchTopBar(
                searchQuery = searchQuery,
                activeFiltersCount = activeFiltersCount,
                onQueryChange = { searchScreenViewModel.updateSearchQuery(it) },
                onBackPressed = onBackPressed,
                onFilterClick = { showFilterSheet = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isInitial = searchQuery.length < 2 && !filterState.hasActiveFilters()

            if (isInitial) {
                MobileSearchInitialView(onOpenFilters = { showFilterSheet = true })
            } else {
                MobileSearchResultsGrid(
                    lazyPagingItems = lazyPagingItems,
                    onAnimeSelected = onAnimeSelected
                )

                if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
                    LoadingIndicator()
                }
            }

            if (showFilterSheet) {
                MobileFilterBottomSheet(
                    filterState = filterState,
                    genreOptions = genreOptions,
                    onFilterChange = { searchScreenViewModel.updateFilterState(it) },
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}
