package dev.daika.davy.feature.player.ui.component

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import dev.daika.davy.core.ui.component.SubtitleOverlay
import dev.daika.davy.feature.player.state.VideoPlayerState

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSurface(
    playerState: VideoPlayerState,
    modifier: Modifier = Modifier,
    subtitleBottomPadding: Dp = 36.dp,
    subtitleFontSize: TextUnit = 18.sp
) {
    val density = LocalDensity.current
    val sourceSizeDp = remember(playerState.videoSizePx, density) {
        playerState.videoSizePx?.let { size ->
            with(density) {
                Size(size.width.toDp().value, size.height.toDp().value)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        key(playerState.videoSizePx) {
            PlayerSurface(
                player = playerState.exoPlayer,
                modifier = Modifier
                    .fillMaxSize()
                    .resizeWithContentScale(
                        contentScale = ContentScale.Fit,
                        sourceSizeDp = sourceSizeDp
                    )
            )
        }

        SubtitleOverlay(
            text = playerState.subtitleText,
            bottomPadding = subtitleBottomPadding,
            fontSize = subtitleFontSize
        )
    }
}