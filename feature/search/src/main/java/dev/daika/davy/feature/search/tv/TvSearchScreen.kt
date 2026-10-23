package dev.daika.davy.feature.search.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.daika.davy.core.common.util.handleDPadKeyEvents
import dev.daika.davy.core.common.util.ifElse
import dev.daika.davy.core.domain.model.ALL_GENRES
import dev.daika.davy.core.domain.model.ALL_SORT_OPTIONS
import dev.daika.davy.core.domain.model.ALL_STATUSES
import dev.daika.davy.core.domain.model.ALL_TYPES
import dev.daika.davy.core.domain.model.ALL_YEARS
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.FilterOption
import dev.daika.davy.core.ui.R
import dev.daika.davy.core.ui.component.TvAnimeItem
import dev.daika.davy.core.ui.component.formatSelectionHint
import dev.daika.davy.feature.search.SearchScreenViewModel

@Composable
fun TvSearchScreen(
    onAnimeSelected: (Anime) -> Unit = {},
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel()
) {
    val lazyPagingItems = searchScreenViewModel.pagedItems.collectAsLazyPagingItems()
    val genreOptions by searchScreenViewModel.genreOptions.collectAsStateWithLifecycle()

    val searchQuery by searchScreenViewModel.searchQuery.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val filterState by searchScreenViewModel.filterState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchScreenViewModel.updateSearchQuery(it)
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.inverseOnSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.inverseSurface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .onFocusChanged {
                        if (it.isFocused)
                            keyboardController?.hide()
                    },
                textStyle = MaterialTheme.typography.titleMedium
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        searchScreenViewModel.updateSearchQuery("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search query",
                    )
                }
            }
            AnimeFilterPanel(
                filterState = filterState,
                genreOptions = genreOptions,
                onFilterStateChange = { searchScreenViewModel.updateFilterState(it) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isNotBlank() || filterState.hasActiveFilters())
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lazyPagingItems.itemCount) { index ->
                    val anime = lazyPagingItems[index]
                    if (anime != null) {
                        TvAnimeItem(
                            anime = anime,
                            modifier = Modifier,
                            onAnimeSelected = onAnimeSelected,
                            index = index
                        )
                    }
                }
            }
    }
}

enum class ActiveFilterPopup {
    INCLUDE_GENRES,
    EXCLUDE_GENRES,
    TYPE,
    STATUS,
    YEAR_FROM,
    YEAR_TO,
    SORT
}

@Composable
fun AnimeFilterPanel(
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    filterState: AnimeFilterState,
    genreOptions: List<FilterOption> = ALL_GENRES,
    onFilterStateChange: ((AnimeFilterState) -> Unit) = {}
) {
    var showFilter by remember { mutableStateOf(initialExpanded) }
    var activePopup by remember { mutableStateOf<ActiveFilterPopup?>(null) }

    if (!showFilter) {
        IconButton(
            onClick = { showFilter = true },
            modifier = modifier
                .size(54.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    if (filterState.hasActiveFilters()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    },
                    RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_filter_alt_24),
                contentDescription = "Expand filters",
                tint = if (filterState.hasActiveFilters()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        Popup(
            alignment = Alignment.TopEnd,
            properties = PopupProperties(focusable = true),
            onDismissRequest = { showFilter = false }) {
            Column(
                modifier = modifier
                    .width(320.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showFilter = false },
                        modifier = Modifier.focusRequester(focusRequester)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_filter_alt_24),
                            contentDescription = "Collapse filters",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "FILTERS",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (filterState.hasActiveFilters()) {
                        IconButton(
                            onClick = { onFilterStateChange(AnimeFilterState()) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Reset all",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                FilterSection(
                    title = "Include genres",
                    hint = formatSelectionHint(
                        selectedIds = filterState.selectedGenres,
                        options = genreOptions,
                        defaultHint = "Select genres to include"
                    ),
                    icon = { FilterIcon(Icons.Default.Add) },
                    onClick = { activePopup = ActiveFilterPopup.INCLUDE_GENRES }
                )
                FilterSection(
                    title = "Exclude genres",
                    hint = formatSelectionHint(
                        selectedIds = filterState.excludedGenres,
                        options = genreOptions,
                        defaultHint = "Select genres to exclude"
                    ),
                    icon = { FilterIcon(Icons.Default.Add) },
                    onClick = { activePopup = ActiveFilterPopup.EXCLUDE_GENRES }
                )
                FilterSection(
                    title = "Anime type",
                    hint = formatSelectionHint(
                        selectedIds = filterState.selectedTypes,
                        options = ALL_TYPES,
                        defaultHint = "Select anime type"
                    ),
                    icon = { FilterIcon(Icons.Default.Add) },
                    onClick = { activePopup = ActiveFilterPopup.TYPE }
                )
                FilterSection(
                    title = "Anime status",
                    hint = formatSelectionHint(
                        selectedIds = filterState.selectedStatuses,
                        options = ALL_STATUSES,
                        defaultHint = "Select anime status"
                    ),
                    icon = { FilterIcon(Icons.Default.Add) },
                    onClick = { activePopup = ActiveFilterPopup.STATUS }
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Year",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterInputField(
                            hint = filterState.yearFrom?.let { "from $it" } ?: "from",
                            modifier = Modifier.weight(1f),
                            onClick = { activePopup = ActiveFilterPopup.YEAR_FROM }
                        )
                        FilterInputField(
                            hint = filterState.yearTo?.let { "to $it" } ?: "to",
                            modifier = Modifier.weight(1f),
                            onClick = { activePopup = ActiveFilterPopup.YEAR_TO }
                        )
                    }
                }

                FilterSection(
                    title = "Sort by",
                    hint = ALL_SORT_OPTIONS.find { it.id == filterState.selectedSort }?.title
                        ?: "Relevance",
                    icon = { FilterIcon(painterResource(R.drawable.outline_sort_24)) },
                    onClick = { activePopup = ActiveFilterPopup.SORT }
                )
            }
        }
    }

    activePopup?.let { popupType ->
        when (popupType) {
            ActiveFilterPopup.INCLUDE_GENRES -> {
                FilterSelectionPopup(
                    title = "Include genres",
                    items = genreOptions,
                    selectedIds = filterState.selectedGenres,
                    isMultipleChoice = true,
                    onApply = { onFilterStateChange(filterState.copy(selectedGenres = it)) },
                    onDismiss = { activePopup = null }
                )
            }

            ActiveFilterPopup.EXCLUDE_GENRES -> {
                FilterSelectionPopup(
                    title = "Exclude genres",
                    items = genreOptions,
                    selectedIds = filterState.excludedGenres,
                    isMultipleChoice = true,
                    onApply = { onFilterStateChange(filterState.copy(excludedGenres = it)) },
                    onDismiss = { activePopup = null }
                )
            }

            ActiveFilterPopup.TYPE -> {
                FilterSelectionPopup(
                    title = "Anime type",
                    items = ALL_TYPES,
                    selectedIds = filterState.selectedTypes,
                    isMultipleChoice = true,
                    onApply = { onFilterStateChange(filterState.copy(selectedTypes = it)) },
                    onDismiss = { activePopup = null }
                )
            }

            ActiveFilterPopup.STATUS -> {
                FilterSelectionPopup(
                    title = "Anime status",
                    items = ALL_STATUSES,
                    selectedIds = filterState.selectedStatuses,
                    isMultipleChoice = true,
                    onApply = { onFilterStateChange(filterState.copy(selectedStatuses = it)) },
                    onDismiss = { activePopup = null }
                )
            }

            ActiveFilterPopup.YEAR_FROM -> {
                FilterSelectionPopup(
                    title = "Year from",
                    items = ALL_YEARS,
                    selectedIds = filterState.yearFrom?.let { setOf(it.toString()) }
                        ?: emptySet(),
                    isMultipleChoice = false,
                    onApply = { selected ->
                        val year = selected.firstOrNull()?.takeIf { it != "none" }?.toIntOrNull()
                        onFilterStateChange(filterState.copy(yearFrom = year))
                    },
                    onDismiss = { activePopup = null }
                )
            }

            ActiveFilterPopup.YEAR_TO -> {
                FilterSelectionPopup(
                    title = "Year to",
                    items = ALL_YEARS,
                    selectedIds = filterState.yearTo?.let { setOf(it.toString()) }
                        ?: emptySet(),
                    isMultipleChoice = false,
                    onApply = { selected ->
                        val year = selected.firstOrNull()?.takeIf { it != "none" }?.toIntOrNull()
                        onFilterStateChange(filterState.copy(yearTo = year))
                    },
                    onDismiss = { activePopup = null }
                )
            }

            ActiveFilterPopup.SORT -> {
                FilterSelectionPopup(
                    title = "Sort by",
                    items = ALL_SORT_OPTIONS,
                    selectedIds = setOf(filterState.selectedSort ?: "none"),
                    isMultipleChoice = false,
                    onApply = { selected ->
                        val sort = selected.firstOrNull()?.takeIf { it != "none" }
                        onFilterStateChange(filterState.copy(selectedSort = sort))
                    },
                    onDismiss = { activePopup = null }
                )
            }
        }
    }
}

@Composable
fun FilterSelectionPopup(
    title: String,
    items: List<FilterOption>,
    selectedIds: Set<String>,
    isMultipleChoice: Boolean,
    onApply: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedIds) }
    val focusRequester = remember { FocusRequester() }
    val topFocusRequester = remember { FocusRequester() }
    val bottomFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .width(380.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isMultipleChoice && tempSelected.isNotEmpty()) {
                                Text(
                                    text = "Selected: ${tempSelected.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.focusRequester(topFocusRequester)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(items) { index, item ->
                            val isSelected = if (isMultipleChoice) {
                                tempSelected.contains(item.id)
                            } else {
                                selectedIds.contains(item.id)
                            }
                            var itemFocused by remember { mutableStateOf(false) }

                            val isInitialFocus = if (isMultipleChoice) {
                                val firstIdx = items.indexOfFirst { tempSelected.contains(it.id) }
                                if (firstIdx >= 0) index == firstIdx else index == 0
                            } else {
                                val firstIdx = items.indexOfFirst { selectedIds.contains(it.id) }
                                if (firstIdx >= 0) index == firstIdx else index == 0
                            }

                            Surface(
                                onClick = {
                                    if (isMultipleChoice) {
                                        tempSelected = if (tempSelected.contains(item.id)) {
                                            tempSelected - item.id
                                        } else {
                                            tempSelected + item.id
                                        }
                                    } else {
                                        if (item.id == "none") {
                                            onApply(emptySet())
                                        } else {
                                            onApply(setOf(item.id))
                                        }
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .ifElse(isInitialFocus, Modifier.focusRequester(focusRequester))
                                    .onFocusChanged { itemFocused = it.isFocused }
                                    .handleDPadKeyEvents(
                                        onLeft = { topFocusRequester.requestFocus() },
                                        onRight = { bottomFocusRequester.requestFocus() }),
                                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        Color.Transparent
                                    },
                                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.85f
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        color = if (itemFocused) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (itemFocused) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isMultipleChoice) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tempSelected.isNotEmpty()) {
                                Surface(
                                    onClick = { tempSelected = emptySet() },
                                    modifier = Modifier.padding(end = 8.dp),
                                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                                    colors = ClickableSurfaceDefaults.colors(
                                        containerColor = Color.Transparent,
                                        focusedContainerColor = MaterialTheme.colorScheme.error.copy(
                                            alpha = 0.3f
                                        )
                                    )
                                ) {
                                    Text(
                                        text = "Reset",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    onApply(tempSelected)
                                    onDismiss()
                                },
                                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.8f
                                    )
                                ),
                                modifier = Modifier.focusRequester(bottomFocusRequester)
                            ) {
                                Text(
                                    text = "Apply",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    hint: String,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        FilterInputField(
            hint = hint,
            icon = icon,
            onClick = onClick
        )
    }
}

@Composable
private fun FilterInputField(
    hint: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isFocused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                },
                RoundedCornerShape(6.dp)
            )
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hint,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            icon()
        }
    }
}

@Composable
private fun FilterIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun FilterIcon(painter: Painter) {
    Icon(
        painter = painter,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
    )
}
