package dev.daika.davy.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.R
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.ui.common.AnimeItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchScreen(
    onAnimeSelected: (Anime) -> Unit = {},
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel()
) {
    val lazyPagingItems = searchScreenViewModel.pagedItems.collectAsLazyPagingItems()

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    searchScreenViewModel.updateSearchQuery(searchQuery)
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search query",
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
            AnimeFilterPanel()
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isNotBlank())
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = spacedBy(8.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {
                items(lazyPagingItems.itemCount) { index ->
                    val anime = lazyPagingItems[index]
                    if (anime != null) {
                        AnimeItem(
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

@Composable
fun AnimeFilterPanel(modifier: Modifier = Modifier) {
    var showFilter by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(12.dp),
        verticalArrangement = spacedBy(14.dp)
    ) {
        Button(
            modifier = Modifier
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    RoundedCornerShape(4.dp)
                )
                .padding(vertical = 12.dp),
            onClick = { showFilter = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_filter_alt_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "РАСКРЫТЬ ФИЛЬТР",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (showFilter) {
            FilterSection(
                title = "Выбрать жанры",
                hint = "По каким жанрам искать",
                icon = { FilterIcon(Icons.Default.Add) }
            )
            FilterSection(
                title = "Исключить жанры",
                hint = "Какие жанры исключить",
                icon = { FilterIcon(Icons.Default.Add) }
            )
            FilterSection(
                title = "Тип аниме",
                hint = "Какой тип аниме искать",
                icon = { FilterIcon(Icons.Default.Add) }
            )
            FilterSection(
                title = "Статус аниме",
                hint = "Статус аниме",
                icon = { FilterIcon(Icons.Default.Add) })

            Column(verticalArrangement = spacedBy(6.dp)) {
                Text(
                    text = "Год",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = spacedBy(8.dp)) {
                    FilterInputField(
                        hint = "от",
//                        modifier = Modifier.weight(1f)
                    )
                    FilterInputField(
                        hint = "до",
//                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FilterSection(
                title = "Сортировать по",
                hint = "Релевантности",
                icon = { FilterIcon(painterResource(R.drawable.outline_sort_24)) }
            )
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
    Column(verticalArrangement = spacedBy(6.dp)) {
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
    Button(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        onClick = onClick
    ) {
        Text(
            text = hint,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        icon()
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

@Preview
@Composable
fun PreviewAnimeFilterPanel() {
    MaterialTheme {
        AnimeFilterPanel(modifier = Modifier.width(350.dp))
    }
}

@Serializable
data object SearchScreenDestination