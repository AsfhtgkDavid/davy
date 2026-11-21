package dev.daika.davy.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.domain.model.AnimeStatus

@Composable
fun AnimeStatusBadge(
    status: AnimeStatus,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val (containerColor, textColor, text) = when (status) {
        AnimeStatus.RELEASED -> Triple(
            Color(0xFF2E7D32).copy(alpha = 0.2f),
            Color(0xFF81C784),
            "Released"
        )
        AnimeStatus.ONGOING -> Triple(
            Color(0xFFE65100).copy(alpha = 0.2f),
            Color(0xFFFFB74D),
            "Ongoing"
        )
        AnimeStatus.ANNOUNCE -> Triple(
            Color(0xFF1565C0).copy(alpha = 0.2f),
            Color(0xFF90CAF9),
            "Announced"
        )
    }

    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}
