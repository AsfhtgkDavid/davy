package dev.daika.davy.ui.screens.player

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import dev.daika.davy.utils.handleDPadKeyEvents
import kotlin.math.pow

const val MAX_LIMIT = 30L * 60L * 1000L

@Composable
fun RowScope.VideoPlayerControllerIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    currentProgress: Long,
    totalDuration: Long,
    onSeek: (seekProgress: Long) -> Unit,
    onSeekPreview: (seekProgress: Long) -> Unit,
    onUserInteraction: () -> Unit,
    onPlayPause: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isSelected by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface

    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp.times((if (isFocused) 2.5f else 1f))
    )
    var seekProgress by remember { mutableLongStateOf(0L) }

    val accelerationState = remember(totalDuration) {
        object {
            var lastEventTime = 0L
            var durationHeld = 0L

            fun calculateDelta(): Long {
                val now = System.currentTimeMillis()
                if (now - lastEventTime > 400L) {
                    durationHeld = 0L
                } else {
                    durationHeld += (now - lastEventTime).coerceAtMost(500L)
                }
                lastEventTime = now

                val secondsPassed = durationHeld.toDouble() / 1000.0
                return (300L * 50.0.pow(secondsPassed)).toLong().coerceAtMost(totalDuration / 10)
            }
        }
    }

    Canvas(
        modifier = modifier
            .weight(1f)
            .height(animatedIndicatorHeight)
            .padding(horizontal = 4.dp)
            .handleDPadKeyEvents(
                onEnter = {
                    if (isSelected) {
                        onSeek(seekProgress)
                        if (!isPlaying)
                            onPlayPause()
                    } else {
                        seekProgress = currentProgress
                        if (isPlaying)
                            onPlayPause()
                    }
                    isSelected = !isSelected
                    onUserInteraction()
                },
                onLeft = {
                    if (!isSelected) {
                        isSelected = true
                        seekProgress = currentProgress
                    }
                    if (isPlaying)
                        onPlayPause()
                    val delta = accelerationState.calculateDelta()
                    seekProgress = (seekProgress - delta).coerceAtLeast(0L)
                    onUserInteraction()
                    onSeekPreview(seekProgress)
                },
                onRight = {
                    if (!isSelected) {
                        isSelected = true
                        seekProgress = currentProgress
                    }
                    if (isPlaying)
                        onPlayPause()
                    val delta = accelerationState.calculateDelta()
                    seekProgress = (seekProgress + delta).coerceAtMost(totalDuration)
                    onUserInteraction()
                    onSeekPreview(seekProgress)
                }
            )
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus) {
                    isSelected = false
                    seekProgress = currentProgress
                    onSeekPreview(seekProgress)
                }
            }
            .focusable(interactionSource = interactionSource),
        onDraw = {
            val yOffset = size.height / 2

            val safeTotalDuration = totalDuration.coerceAtLeast(1L).toFloat()
            val currentPosition = if (isSelected) seekProgress else currentProgress
            val progressFraction = currentPosition.toFloat() / safeTotalDuration

            drawLine(
                color = color.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width, y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(x = 0f, y = yOffset),
                end = Offset(
                    x = size.width * progressFraction,
                    y = yOffset
                ),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    )
}
