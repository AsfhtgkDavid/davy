package dev.daika.davy.feature.search.ui.tv

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.daika.davy.core.ui.component.EmptyState

@Composable
fun TvSearchInitialView(modifier: Modifier = Modifier) {
    EmptyState(
        title = "Catalog search",
        subtitle = "Enter anime title or configure filters to search",
        icon = Icons.Default.Search,
        modifier = modifier
    )
}