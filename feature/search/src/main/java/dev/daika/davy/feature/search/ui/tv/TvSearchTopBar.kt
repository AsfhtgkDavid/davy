package dev.daika.davy.feature.search.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.FilterOption

@Composable
fun TvSearchTopBar(
    searchQuery: String,
    filterState: AnimeFilterState,
    genreOptions: List<FilterOption>,
    onQueryChange: (String) -> Unit,
    onFilterStateChange: (AnimeFilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
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
                    if (it.isFocused) keyboardController?.hide()
                },
            textStyle = MaterialTheme.typography.titleMedium
        )

        if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search query"
                )
            }
        }

        AnimeFilterPanel(
            filterState = filterState,
            genreOptions = genreOptions,
            onFilterStateChange = onFilterStateChange
        )
    }
}
