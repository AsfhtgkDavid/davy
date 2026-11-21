package dev.daika.davy.feature.player.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun TvPlayerTopBar(
    animeTitle: String,
    episodeNumber: String,
    modifier: Modifier = Modifier
) {
    val maxTitleLength = 25
    val displayTitle = if (animeTitle.length > maxTitleLength) {
        "${animeTitle.take(maxTitleLength / 2)}...${animeTitle.takeLast(maxTitleLength / 2)}"
    } else {
        animeTitle
    }

    Text(
        text = "$displayTitle - ep $episodeNumber",
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
