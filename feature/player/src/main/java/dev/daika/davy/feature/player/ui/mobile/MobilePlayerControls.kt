package dev.daika.davy.feature.player.ui.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.daika.davy.feature.player.ui.component.VideoPlayerState

@Composable
fun MobilePlayerControls(
    playerState: VideoPlayerState,
    animeTitle: String,
    episodeNumber: String,
    isVisible: Boolean,
    onBackPressed: () -> Unit,
    onQualityClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            MobilePlayerTopBar(
                animeTitle = animeTitle,
                episodeNumber = episodeNumber,
                onBackPressed = onBackPressed,
                modifier = Modifier.align(Alignment.TopStart)
            )

            MobilePlayerCenterControls(
                isPlaying = playerState.isPlaying,
                onPlayPause = {
                    onUserInteraction()
                    playerState.togglePlayPause()
                },
                onRewind10 = {
                    onUserInteraction()
                    playerState.seekTo(playerState.currentPosition - 10000L)
                },
                onForward10 = {
                    onUserInteraction()
                    playerState.seekTo(playerState.currentPosition + 10000L)
                },
                onPrevEpisode = {
                    onUserInteraction()
                    playerState.playPreviousEpisode()
                },
                onNextEpisode = {
                    onUserInteraction()
                    playerState.playNextEpisode()
                },
                modifier = Modifier.align(Alignment.Center)
            )

            MobilePlayerBottomBar(
                currentPosition = playerState.currentPosition,
                videoDuration = playerState.videoDuration,
                isDraggingSlider = playerState.isDraggingSlider,
                sliderProgress = playerState.sliderProgress,
                hasSubtitles = playerState.availableSubtitles.isNotEmpty(),
                hasActiveSubtitles = playerState.selectedSubtitles != null,
                onSliderValueChange = {
                    playerState.isDraggingSlider = true
                    playerState.sliderProgress = it
                    onUserInteraction()
                },
                onSliderValueChangeFinished = {
                    val safeTotal = playerState.videoDuration.coerceAtLeast(1L).toFloat()
                    val targetPosition = (playerState.sliderProgress * safeTotal).toLong()
                    playerState.seekTo(targetPosition)
                    playerState.isDraggingSlider = false
                    onUserInteraction()
                },
                onQualityClick = {
                    onUserInteraction()
                    onQualityClick()
                },
                onSubtitlesClick = {
                    onUserInteraction()
                    onSubtitlesClick()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
