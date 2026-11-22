package dev.daika.davy.feature.details.ui.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.ui.R
import dev.daika.davy.core.ui.component.MobileAnimeCard

@Composable
fun MobileViewingOrderSection(
    viewingOrder: List<Anime>,
    onAnimeSelected: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    if (viewingOrder.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.details_viewing_order),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewingOrder, key = { it.id }) { relatedAnime ->
                MobileAnimeCard(
                    anime = relatedAnime,
                    onClick = { onAnimeSelected(relatedAnime) },
                    modifier = Modifier.width(130.dp)
                )
            }
        }
    }
}
