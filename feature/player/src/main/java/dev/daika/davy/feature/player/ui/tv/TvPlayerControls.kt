package dev.daika.davy.feature.player.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.daika.davy.core.common.util.formatTime
import dev.daika.davy.core.ui.R
import dev.daika.davy.feature.player.state.VideoPlayerState

@Composable
fun TvPlayerControls(
    playerState: VideoPlayerState,
    animeTitle: String,
    episodeNumber: String,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQualityOptions by remember { mutableStateOf(false) }
    var showSubtitleOptions by remember { mutableStateOf(false) }
    var positionPreview by remember { mutableStateOf<Long?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            TvPlayerTopBar(
                animeTitle = animeTitle,
                episodeNumber = episodeNumber,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTime(positionPreview ?: playerState.currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )

                VideoPlayerControllerIndicator(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(horizontal = 2.dp),
                    isPlaying = playerState.isPlaying,
                    currentProgress = playerState.currentPosition,
                    totalDuration = playerState.videoDuration,
                    onSeek = { position ->
                        playerState.seekTo(position)
                        positionPreview = null
                    },
                    onSeekPreview = { positionPreview = it },
                    onUserInteraction = onUserInteraction,
                    onPlayPause = { playerState.togglePlayPause() }
                )

                Text(
                    text = formatTime(playerState.videoDuration),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onUserInteraction()
                    playerState.playPreviousEpisode()
                }) {
                    Icon(
                        painterResource(R.drawable.baseline_skip_previous_24),
                        contentDescription = stringResource(R.string.cd_previous_episode)
                    )
                }

                IconButton(onClick = {
                    onUserInteraction()
                    playerState.togglePlayPause()
                }) {
                    Icon(
                        if (playerState.isPlaying) painterResource(R.drawable.baseline_pause_24) else painterResource(
                            R.drawable.baseline_play_arrow_24
                        ),
                        contentDescription = if (playerState.isPlaying) stringResource(R.string.cd_pause) else stringResource(
                            R.string.cd_play
                        )
                    )
                }

                IconButton(onClick = {
                    onUserInteraction()
                    playerState.playNextEpisode()
                }) {
                    Icon(
                        painterResource(R.drawable.baseline_skip_next_24),
                        contentDescription = stringResource(R.string.cd_next_episode)
                    )
                }

                IconButton(onClick = {
                    onUserInteraction()
                    showQualityOptions = true
                }) {
                    Icon(
                        painterResource(R.drawable.outline_high_quality_24),
                        contentDescription = stringResource(R.string.cd_quality_settings)
                    )
                }

                if (playerState.availableSubtitles.isNotEmpty()) {
                    IconButton(onClick = {
                        onUserInteraction()
                        showSubtitleOptions = true
                    }) {
                        Icon(
                            painterResource(R.drawable.outline_subtitles_24),
                            contentDescription = stringResource(R.string.cd_subtitles_settings)
                        )
                    }
                }
            }

            if (showQualityOptions) {
                val qualityOptions = playerState.availableResolutions.map { it.quality }
                val selectedIndex = playerState.availableResolutions.indexOfFirst {
                    it.src.contains(playerState.selectedSource.src)
                }.coerceAtLeast(0)

                SelectionMenuPopup(
                    title = stringResource(R.string.player_select_quality),
                    options = qualityOptions,
                    selectedIndex = selectedIndex,
                    onSelect = { index ->
                        playerState.selectSource(playerState.availableResolutions[index])
                        showQualityOptions = false
                        onUserInteraction()
                    },
                    onDismiss = {
                        showQualityOptions = false
                        onUserInteraction()
                    }
                )
            }

            if (showSubtitleOptions) {
                val subtitleOptions =
                    playerState.availableSubtitles.map { it.label }.toMutableList()
                val selectedIndex = playerState.availableSubtitles.indexOfFirst {
                    it.src == playerState.selectedSubtitles?.src
                } + 1
                subtitleOptions.add(0, stringResource(R.string.player_turn_off_subtitles))

                SelectionMenuPopup(
                    title = stringResource(R.string.player_select_subtitles),
                    options = subtitleOptions,
                    selectedIndex = selectedIndex,
                    onSelect = { index ->
                        if (index == 0) {
                            playerState.selectSubtitles(null)
                        } else {
                            playerState.selectSubtitles(playerState.availableSubtitles[index - 1])
                        }
                        showSubtitleOptions = false
                        onUserInteraction()
                    },
                    onDismiss = {
                        showSubtitleOptions = false
                        onUserInteraction()
                    }
                )
            }
        }
    }
}
