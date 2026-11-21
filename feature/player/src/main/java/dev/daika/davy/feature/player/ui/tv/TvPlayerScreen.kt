package dev.daika.davy.feature.player.ui.tv

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import dev.daika.davy.core.ui.component.ErrorView
import dev.daika.davy.core.ui.component.LoadingIndicator
import dev.daika.davy.feature.player.PlayerScreenUiState
import dev.daika.davy.feature.player.PlayerScreenViewModel
import dev.daika.davy.feature.player.ui.component.VideoPlayerSurface
import dev.daika.davy.feature.player.ui.component.rememberVideoPlayerState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
fun TvPlayerScreen(
    onBackPressed: () -> Unit = {},
    playerScreenViewModel: PlayerScreenViewModel = hiltViewModel()
) {
    val uiState by playerScreenViewModel.uiState.collectAsState()

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
            var resetControlsTimer by remember { mutableIntStateOf(0) }

            BackHandler(enabled = isControlsVisible) {
                isControlsVisible = false
            }

            LaunchedEffect(isControlsVisible, playerState.isPlaying, resetControlsTimer) {
                if (isControlsVisible && playerState.isPlaying) {
                    delay(5.seconds)
                    isControlsVisible = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.key != Key.Back) {
                            resetControlsTimer++
                            if (!isControlsVisible) {
                                isControlsVisible = true
                                return@onKeyEvent true
                            }
                        }
                        false
                    }
            ) {
                VideoPlayerSurface(
                    playerState = playerState,
                    subtitleBottomPadding = if (isControlsVisible) 100.dp else 24.dp,
                    subtitleFontSize = 20.sp
                )

                if (currentState.isEpisodeLoading) {
                    LoadingIndicator()
                } else if (isControlsVisible) {
                    TvPlayerControls(
                        playerState = playerState,
                        animeTitle = currentState.animeTitle,
                        episodeNumber = currentState.episodeNumber,
                        onUserInteraction = { resetControlsTimer++ }
                    )
                }
            }
        }

        is PlayerScreenUiState.Error -> {
            ErrorView(
                title = "Unable to load player",
                message = currentState.message,
                onBackPressed = onBackPressed
            )
        }
    }
}
