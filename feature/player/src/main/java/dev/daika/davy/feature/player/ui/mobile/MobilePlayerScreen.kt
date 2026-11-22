package dev.daika.davy.feature.player.ui.mobile

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import dev.daika.davy.core.ui.util.findActivity
import dev.daika.davy.core.ui.R
import dev.daika.davy.core.ui.component.ErrorView
import dev.daika.davy.core.ui.util.HideBars
import dev.daika.davy.core.ui.util.KeepScreenOn
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.feature.player.PlayerScreenUiState
import dev.daika.davy.feature.player.PlayerScreenViewModel
import dev.daika.davy.feature.player.ui.component.VideoPlayerSurface
import dev.daika.davy.feature.player.state.rememberVideoPlayerState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
fun MobilePlayerScreen(
    onBackPressed: () -> Unit,
    playerScreenViewModel: PlayerScreenViewModel = hiltViewModel()
) {
    val uiState by playerScreenViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    DisposableEffect(activity) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    when (val currentState = uiState) {
        is PlayerScreenUiState.Loading -> {
            LoadingIndicator()
        }

        is PlayerScreenUiState.Success -> {
            val playerState = rememberVideoPlayerState(
                dataSourceFactory = playerScreenViewModel.dataSourceFactory,
                availableResolutions = currentState.availableResolutions,
                availableSubtitles = currentState.availableSubtitles,
                episode = currentState.episodeNumber,
                onResumed = { playerScreenViewModel.onResumed(it) },
                onPaused = { playerScreenViewModel.onPaused(it) },
                onSeeked = { playerScreenViewModel.onSeeked(it) },
                onTrackChanged = { time, res, subId ->
                    playerScreenViewModel.onTrackChanged(time, res, subId)
                },
                playNextEpisode = { playerScreenViewModel.playNextEpisode() },
                playPreviousEpisode = { playerScreenViewModel.playPreviousEpisode() }
            )

            var isControlsVisible by remember { mutableStateOf(true) }
            var userInteractionCount by remember { mutableIntStateOf(0) }
            var showQualitySheet by remember { mutableStateOf(false) }
            var showSubtitleSheet by remember { mutableStateOf(false) }

            BackHandler {
                onBackPressed()
            }

            LaunchedEffect(isControlsVisible, playerState.isPlaying, userInteractionCount) {
                if (isControlsVisible && playerState.isPlaying && !playerState.isDraggingSlider) {
                    delay(4.seconds)
                    isControlsVisible = false
                }
            }

            HideBars()
            KeepScreenOn(playerState.isPlaying)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isControlsVisible = !isControlsVisible
                        userInteractionCount++
                    },
                contentAlignment = Alignment.Center
            ) {
                VideoPlayerSurface(
                    playerState = playerState,
                    subtitleBottomPadding = if (isControlsVisible) 110.dp else 36.dp
                )
                if (currentState.isEpisodeLoading) {
                    LoadingIndicator()
                } else {
                    MobilePlayerControls(
                        playerState = playerState,
                        animeTitle = currentState.animeTitle,
                        episodeNumber = currentState.episodeNumber,
                        isVisible = isControlsVisible,
                        onBackPressed = onBackPressed,
                        onQualityClick = { showQualitySheet = true },
                        onSubtitlesClick = { showSubtitleSheet = true },
                        onUserInteraction = { userInteractionCount++ }
                    )

                    if (showQualitySheet) {
                        MobileQualitySheet(
                            availableResolutions = playerState.availableResolutions,
                            selectedSource = playerState.selectedSource,
                            onResolutionSelected = { resolution ->
                                playerState.selectSource(resolution)
                            },
                            onDismiss = { showQualitySheet = false }
                        )
                    }

                    if (showSubtitleSheet) {
                        MobileSubtitleSheet(
                            availableSubtitles = playerState.availableSubtitles,
                            selectedSubtitles = playerState.selectedSubtitles,
                            onSubtitleSelected = { subtitle ->
                                playerState.selectSubtitles(subtitle)
                            },
                            onDismiss = { showSubtitleSheet = false }
                        )
                    }
                }
            }
        }

        is PlayerScreenUiState.Error -> {
            ErrorView(
                title = stringResource(R.string.error_playback),
                message = currentState.message,
                onBackPressed = onBackPressed
            )
        }
    }
}
