package dev.daika.davy.feature.player.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.daika.davy.feature.player.Subtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileSubtitleSheet(
    availableSubtitles: List<Subtitle>,
    selectedSubtitles: Subtitle?,
    onSubtitleSelected: (Subtitle?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Subtitles",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                onClick = {
                    onSubtitleSelected(null)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (selectedSubtitles == null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ) {
                Text(
                    text = "Off",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (selectedSubtitles == null) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selectedSubtitles == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            availableSubtitles.forEach { subtitle ->
                val isSelected = subtitle == selectedSubtitles
                Surface(
                    onClick = {
                        onSubtitleSelected(subtitle)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ) {
                    Text(
                        text = subtitle.label,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
