package dev.daika.davy.feature.search.ui.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.domain.model.ALL_SORT_OPTIONS
import dev.daika.davy.core.domain.model.ALL_STATUSES
import dev.daika.davy.core.domain.model.ALL_TYPES
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.FilterOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileFilterBottomSheet(
    filterState: AnimeFilterState,
    genreOptions: List<FilterOption>,
    onFilterChange: (AnimeFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var tempState by remember { mutableStateOf(filterState) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (tempState.hasActiveFilters()) {
                    Text(
                        text = "Reset all",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clickable { tempState = AnimeFilterState() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_SORT_OPTIONS.forEach { sort ->
                    val isSelected = (tempState.selectedSort ?: "none") == sort.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempState = tempState.copy(
                                selectedSort = if (sort.id == "none") null else sort.id
                            )
                        },
                        label = { Text(sort.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Anime status",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_STATUSES.forEach { status ->
                    val isSelected = tempState.selectedStatuses.contains(status.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = if (isSelected) {
                                tempState.selectedStatuses - status.id
                            } else {
                                tempState.selectedStatuses + status.id
                            }
                            tempState = tempState.copy(selectedStatuses = newSet)
                        },
                        label = { Text(status.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Anime type",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_TYPES.forEach { type ->
                    val isSelected = tempState.selectedTypes.contains(type.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = if (isSelected) {
                                tempState.selectedTypes - type.id
                            } else {
                                tempState.selectedTypes + type.id
                            }
                            tempState = tempState.copy(selectedTypes = newSet)
                        },
                        label = { Text(type.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (genreOptions.isNotEmpty()) {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genreOptions.forEach { genre ->
                        val isSelected = tempState.selectedGenres.contains(genre.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newSet = if (isSelected) {
                                    tempState.selectedGenres - genre.id
                                } else {
                                    tempState.selectedGenres + genre.id
                                }
                                tempState = tempState.copy(selectedGenres = newSet)
                            },
                            label = { Text(genre.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    onFilterChange(tempState)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Apply filters",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
