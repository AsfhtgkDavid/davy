package dev.daika.davy.feature.search.ui.mobile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.ui.component.EmptyState

@Composable
fun MobileSearchInitialView(
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "Catalog search",
        subtitle = "Enter anime title or configure filters to search",
        icon = Icons.Default.Search,
        action = {
            OutlinedButton(onClick = onOpenFilters) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open filters")
            }
        },
        modifier = modifier
    )
}
