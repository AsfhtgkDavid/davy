package dev.daika.davy.ui.screens.player

import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import dev.daika.davy.R
import dev.daika.davy.ui.common.SelectionMenuPopup
import dev.daika.davy.utils.formatTime
import dev.daika.davyparsers.PlayerData
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import androidx.core.net.toUri

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(playerScreenViewModel: PlayerScreenViewModel = hiltViewModel()) {
    val uiState by playerScreenViewModel.uiState.collectAsState()

    when (uiState) {
        is PlayerScreenUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PlayerScreenUiState.Success -> {
            val playerData = (uiState as PlayerScreenUiState.Success).playerData
            val context =
                if (Build.VERSION.SDK_INT >= 30) LocalContext.current.createAttributionContext("playback") else LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            var isControlsVisible by remember { mutableStateOf(true) }
            var isPlaying by remember { mutableStateOf(false) }

            var currentPosition by remember { mutableLongStateOf(0L) }
            var resetControlsTimer by remember { mutableIntStateOf(0) }

            var selectedSource by remember {
                mutableStateOf((playerData.translations.firstOrNull { it.isDefault }
                    ?: (playerData.translations.first())).streams.maxByOrNull { it.quality.toInt() }!!.urls.first())
            }
            var selectedSubtitles by remember {
                mutableStateOf(playerData.subtitles.firstOrNull { it.isDefault }?.src)
            }
            var savedPosition by remember { mutableLongStateOf(0L) }
            var videoDuration by remember { mutableLongStateOf(1L) }
            var playWhenReadyState by remember { mutableStateOf(true) }
            var subtitleText by remember { mutableStateOf("") }
            var currentTracks by remember { mutableStateOf(Tracks.EMPTY) }

            val dataSourceFactory = DefaultDataSource.Factory(
                context,
                (uiState as PlayerScreenUiState.Success).dataSourceFactory
            )
            val exoPlayer = remember {
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                    .build()
                    .apply {
                        playWhenReady = true
                    }
            }

            fun setItem(
                mediaItem: MediaItem
            ) {
                var savedPosition1 = savedPosition
                var playWhenReadyState1 = playWhenReadyState
                if (exoPlayer.mediaItemCount > 0) {
                    savedPosition1 = exoPlayer.currentPosition
                    playWhenReadyState1 = exoPlayer.playWhenReady
                }

                exoPlayer.replaceMediaItem(0, mediaItem)
                exoPlayer.prepare()

                exoPlayer.seekTo(savedPosition1)
                exoPlayer.playWhenReady = playWhenReadyState1
            }

            BackHandler(enabled = isControlsVisible) {
                isControlsVisible = false
            }

            LaunchedEffect(isControlsVisible, isPlaying, resetControlsTimer) {
                if (isControlsVisible && isPlaying) {
                    delay(5.seconds)
                    isControlsVisible = false
                }
            }

            LaunchedEffect(isPlaying, isControlsVisible) {
                while (isControlsVisible) {
                    currentPosition = exoPlayer.currentPosition
                    delay(1.seconds)
                }
            }

            LaunchedEffect(selectedSource) {
                subtitleText = ""

                val subtitleConfigs = playerData.subtitles.map { subtitle ->
                    MediaItem.SubtitleConfiguration.Builder(subtitle.src.toUri())
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setId(subtitle.src)
                        .setLabel(subtitle.label)
                        .build()
                }

                val mediaItem = MediaItem.Builder()
                    .setUri(selectedSource)
                    .setSubtitleConfigurations(subtitleConfigs)
                    .build()

                setItem(mediaItem)
            }

            LaunchedEffect(selectedSubtitles, currentTracks) {
                val trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()

                if (selectedSubtitles == null) {
                    trackSelectionParameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    trackSelectionParameters.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                } else {
                    trackSelectionParameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)

                    currentTracks.groups.find { trackGroup ->
                        trackGroup.mediaTrackGroup.getFormat(0).id?.split(":")?.drop(1)
                            ?.joinToString(":") == selectedSubtitles
                    }?.let { textTrackGroup ->
                        trackSelectionParameters.setOverrideForType(
                            TrackSelectionOverride(textTrackGroup.mediaTrackGroup, 0)
                        )
                    }
                }

                exoPlayer.trackSelectionParameters = trackSelectionParameters.build()
            }

            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            videoDuration = exoPlayer.contentDuration.coerceAtLeast(1L)
                        } else if (playbackState == Player.STATE_ENDED) {
                            playerScreenViewModel.playNextEpisode()
                        }
                    }

                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        isPlaying = isPlayingState
                    }

                    override fun onCues(cueGroup: CueGroup) {
                        subtitleText = cueGroup.cues.firstOrNull()?.text?.toString() ?: ""
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        currentTracks = tracks
                    }
                }
                exoPlayer.addListener(listener)

                currentTracks = exoPlayer.currentTracks
                isPlaying = exoPlayer.isPlaying
                if (exoPlayer.playbackState == Player.STATE_READY) {
                    videoDuration = exoPlayer.contentDuration.coerceAtLeast(1L)
                }

                onDispose {
                    exoPlayer.removeListener(listener)
                }
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> {
                            exoPlayer.pause()
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            if (playWhenReadyState) {
                                exoPlayer.play()
                            }
                        }

                        else -> {}
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    exoPlayer.release()
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
                key(selectedSource) {
                    PlayerSurface(
                        player = exoPlayer,
                        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                        modifier = Modifier
                            .fillMaxSize()
                            .resizeWithContentScale(
                                contentScale = ContentScale.Fit,
                                sourceSizeDp = null
                            )
                    )
                }
                if (subtitleText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (isControlsVisible) 100.dp else 24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = subtitleText,
                            color = Color.White,
                            fontSize = 20.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                if (isControlsVisible) {
                    PlayerControls(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        videoDuration = videoDuration,
                        playerData = playerData,
                        onPlayPause = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        onNextEpisode = { playerScreenViewModel.playNextEpisode() },
                        onPrevEpisode = { playerScreenViewModel.playPreviousEpisode() },
                        onSeek = { position ->
                            exoPlayer.seekTo(position)
                            currentPosition = position
                        },
                        onQualitySelect = { urls -> selectedSource = urls.first() },
                        onSubtitleSelect = { url -> selectedSubtitles = url },
                        onUserInteraction = { resetControlsTimer++ },
                        currentQuality = selectedSource,
                        currentSubtitles = selectedSubtitles
                    )
                }
            }
        }

        is PlayerScreenUiState.Error -> {
            // Show error message
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    videoDuration: Long,
    playerData: PlayerData,
    currentQuality: String,
    currentSubtitles: String?,
    onPlayPause: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onSeek: (Long) -> Unit,
    onQualitySelect: (List<String>) -> Unit,
    onSubtitleSelect: (String?) -> Unit,
    onUserInteraction: () -> Unit
) {
    var showQualityOptions by remember { mutableStateOf(false) }
    var showSubtitleOptions by remember { mutableStateOf(false) }

    var positionPreview by remember { mutableStateOf<Long?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTime(positionPreview ?: currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )

                VideoPlayerControllerIndicator(
                    modifier = Modifier.focusRequester(focusRequester),
                    isPlaying = isPlaying,
                    currentProgress = currentPosition,
                    totalDuration = videoDuration,
                    onSeek = { position ->
                        onSeek(position)
                        positionPreview = null
                    },
                    onSeekPreview = { positionPreview = it },
                    onUserInteraction = onUserInteraction,
                    onPlayPause = onPlayPause
                )

                Text(
                    text = formatTime(videoDuration),
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
                IconButton(onClick = onPrevEpisode) {
                    Icon(
                        painterResource(R.drawable.baseline_skip_previous_24),
                        contentDescription = "Previous Episode"
                    )
                }
                IconButton(
                    onClick = onPlayPause,
                ) {
                    Icon(
                        if (isPlaying) painterResource(R.drawable.baseline_pause_24) else painterResource(
                            R.drawable.baseline_play_arrow_24
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
                IconButton(onClick = onNextEpisode) {
                    Icon(
                        painterResource(R.drawable.baseline_skip_next_24),
                        contentDescription = "Next Episode"
                    )
                }
                IconButton(onClick = { showQualityOptions = true }) {
                    Icon(
                        painterResource(R.drawable.outline_high_quality_24),
                        contentDescription = "Quality Settings"
                    )
                }

                if (playerData.subtitles.isNotEmpty())
                    IconButton(onClick = { showSubtitleOptions = true }) {
                        Icon(
                            painterResource(R.drawable.outline_subtitles_24),
                            contentDescription = "Subtitles Settings"
                        )
                    }
            }
            if (showQualityOptions) {
                val streams = (playerData.translations.firstOrNull { it.isDefault }
                    ?: playerData.translations.first()).streams.sortedBy { it.quality.toInt() }
                val qualityOptions = streams.map { it.quality }
                val selectedIndex =
                    streams.indexOfFirst { it.urls.contains(currentQuality) }.coerceAtLeast(0)

                SelectionMenuPopup(
                    title = "Select Quality",
                    options = qualityOptions,
                    selectedIndex = selectedIndex,
                    onSelect = { index ->
                        onQualitySelect(streams[index].urls)
                        showQualityOptions = false
                    },
                    onDismiss = { showQualityOptions = false }
                )
            }

            if (showSubtitleOptions) {
                val subtitles = playerData.subtitles
                val subtitleOptions = subtitles.map { it.label }.toMutableList()
                val selectedIndex =
                    subtitles.indexOfFirst { it.src == currentSubtitles } + 1
                subtitleOptions.add(0, "Turn Off")

                SelectionMenuPopup(
                    title = "Select Subtitles",
                    options = subtitleOptions,
                    selectedIndex = selectedIndex,
                    onSelect = { index ->
                        if (index == 0)
                            onSubtitleSelect(null)
                        else
                            onSubtitleSelect(subtitles[index - 1].src)
                        showSubtitleOptions = false
                    },
                    onDismiss = { showSubtitleOptions = false }
                )
            }
        }
    }
}

@Serializable
data class PlayerScreenDestination(
    val animeId: Int,
    val episodeId: Int
)