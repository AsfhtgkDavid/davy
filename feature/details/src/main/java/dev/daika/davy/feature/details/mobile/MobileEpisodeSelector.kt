package dev.daika.davy.feature.details.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.domain.model.AnimeTranslation

@Composable
fun MobileEpisodeSelector(
    translations: List<AnimeTranslation>,
    onEpisodeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (translations.isEmpty()) return

    var selectedTranslation by remember { mutableStateOf(translations.firstOrNull()) }
    var selectedPlayer by remember { mutableStateOf(selectedTranslation?.availablePlayers?.firstOrNull()) }
    var isTranslationDropdownOpen by remember { mutableStateOf(false) }
    var isPlayerDropdownOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTranslation) {
        selectedPlayer = selectedTranslation?.availablePlayers?.firstOrNull()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Voiceover & Episodes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Translation Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { isTranslationDropdownOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = selectedTranslation?.title ?: "Select voiceover",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = isTranslationDropdownOpen,
                    onDismissRequest = { isTranslationDropdownOpen = false }
                ) {
                    translations.forEach { translation ->
                        DropdownMenuItem(
                            text = {
                                Text("${translation.title} (${translation.availablePlayers.maxOfOrNull { it.episodes.size } ?: 0} eps)")
                            },
                            onClick = {
                                selectedTranslation = translation
                                isTranslationDropdownOpen = false
                            }
                        )
                    }
                }
            }

            val availablePlayers = selectedTranslation?.availablePlayers ?: emptyList()
            if (availablePlayers.size > 1) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { isPlayerDropdownOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = selectedPlayer?.player ?: "Player",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = isPlayerDropdownOpen,
                        onDismissRequest = { isPlayerDropdownOpen = false }
                    ) {
                        availablePlayers.forEach { player ->
                            DropdownMenuItem(
                                text = { Text(player.player) },
                                onClick = {
                                    selectedPlayer = player
                                    isPlayerDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val episodes = selectedPlayer?.episodes ?: emptyList()
        if (episodes.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                episodes.forEach { episode ->
                    Surface(
                        onClick = { onEpisodeSelected(episode.videoId) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(width = 64.dp, height = 40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = episode.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
