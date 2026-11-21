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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.daika.davy.core.domain.model.ALL_SORT_OPTIONS
import dev.daika.davy.core.domain.model.ALL_STATUSES
import dev.daika.davy.core.domain.model.ALL_TYPES
import dev.daika.davy.core.domain.model.ALL_YEARS
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.FilterOption
import dev.daika.davy.core.ui.R
import dev.daika.davy.core.ui.component.formatSelectionHint

@Composable
fun AnimeFilterPanel(
    filterState: AnimeFilterState,
    genreOptions: List<FilterOption>,
    onFilterStateChange: (AnimeFilterState) -> Unit
) {
    var showFilter by remember { mutableStateOf(false) }
    var activePopup by remember { mutableStateOf<ActiveFilterPopup?>(null) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        onClick = { showFilter = true },
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (filterState.hasActiveFilters()) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            focusedContainerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.height(54.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_filter_alt_24),
                contentDescription = "Filters",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Filters",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (showFilter) {
        LaunchedEffect(Unit) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
        Popup(
            alignment = Alignment.CenterEnd,
            onDismissRequest = { showFilter = false },
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showFilter = false }
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(360.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
}
