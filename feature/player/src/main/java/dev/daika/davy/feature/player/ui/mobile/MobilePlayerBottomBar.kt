package dev.daika.davy.feature.player.ui.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.common.util.formatTime

@Composable
fun MobilePlayerBottomBar(
    currentPosition: Long,
    videoDuration: Long,
    isDraggingSlider: Boolean,
    sliderProgress: Float,
    hasSubtitles: Boolean,
    hasActiveSubtitles: Boolean,
    onSliderValueChange: (Float) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
    onQualityClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val safeTotal = videoDuration.coerceAtLeast(1L).toFloat()
        val progressValue = if (isDraggingSlider) {
            sliderProgress
        } else {
            (currentPosition.toFloat() / safeTotal).coerceIn(0f, 1f)
        }

        Slider(
            value = progressValue,
            onValueChange = onSliderValueChange,
            onValueChangeFinished = onSliderValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentDisplay = if (isDraggingSlider) {
                (sliderProgress * safeTotal).toLong()
            } else {
                currentPosition
            }

            Text(
                text = "${formatTime(currentDisplay)} / ${formatTime(videoDuration)}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onQualityClick) {
                    Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = "Quality",
                        tint = Color.White
                    )
                }

                if (hasSubtitles) {
                    IconButton(onClick = onSubtitlesClick) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitles",
                            tint = if (hasActiveSubtitles) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                }
            }
        }
    }
}
