package dev.daika.davy.feature.search.ui.tv

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.daika.davy.core.ui.R
import dev.daika.davy.core.ui.component.EmptyState

@Composable
fun TvSearchInitialView(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.search_catalog_title),
        subtitle = stringResource(R.string.search_catalog_subtitle),
        icon = Icons.Default.Search,
        modifier = modifier
    )
}