package dev.daika.davy.feature.player.ui.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.daika.davy.core.ui.R

@Composable
fun MobilePlayerCenterControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPrevEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevEpisode) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.cd_previous_episode),
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(onClick = onRewind10) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = stringResource(R.string.cd_rewind_10),
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(64.dp)
                .clickable { onPlayPause() }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        IconButton(onClick = onForward10) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = stringResource(R.string.cd_forward_10),
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(onClick = onNextEpisode) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.cd_next_episode),
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
