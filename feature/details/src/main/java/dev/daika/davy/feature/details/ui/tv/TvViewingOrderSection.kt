package dev.daika.davy.feature.details.ui.tv

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.component.TvAnimeRow

@Composable
fun TvViewingOrderSection(
    viewingOrder: List<Anime>,
    onAnimeSelected: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    if (viewingOrder.isEmpty()) return

    TvAnimeRow(
        modifier = modifier.padding(top = 16.dp),
        animeList = viewingOrder,
        title = "Viewing Order",
        onAnimeSelected = onAnimeSelected
    )
}
