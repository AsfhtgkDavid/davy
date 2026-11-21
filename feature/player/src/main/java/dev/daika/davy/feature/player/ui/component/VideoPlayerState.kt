package dev.daika.davy.feature.player.ui.component

import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.util.EventLogger
import dev.daika.davy.feature.player.Resolution
import dev.daika.davy.feature.player.Subtitle
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Stable
class VideoPlayerState(
    val exoPlayer: ExoPlayer,
    initialResolutions: List<Resolution>,
    initialSubtitles: List<Subtitle>,
    val onResumed: (Long) -> Unit,
    val onPaused: (Long) -> Unit,
    val onSeeked: (Long) -> Unit,
    val onTrackChanged: (currentTime: Long, resolution: String, subtitleId: Int) -> Unit,
    val playNextEpisode: () -> Unit,
    val playPreviousEpisode: () -> Unit
) {
    var availableResolutions by mutableStateOf(initialResolutions)
    var availableSubtitles by mutableStateOf(initialSubtitles)

    var selectedSource by mutableStateOf(initialResolutions.last())
    var selectedSubtitles by mutableStateOf(initialSubtitles.firstOrNull { it.default })

    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var videoDuration by mutableLongStateOf(1L)
    var subtitleText by mutableStateOf("")
    var currentTracks by mutableStateOf(Tracks.EMPTY)
    var videoSizePx by mutableStateOf<Size?>(null)

    var playWhenReadyState by mutableStateOf(true)
    var hasStartedPlayback by mutableStateOf(false)

    var isDraggingSlider by mutableStateOf(false)
    var sliderProgress by mutableFloatStateOf(0f)

    private var preservePositionOnNextSourceChange = false

    fun play() {
        playWhenReadyState = true
        exoPlayer.play()
    }

    fun pause() {
        playWhenReadyState = false
        exoPlayer.pause()
    }

    fun togglePlayPause() {
        if (isPlaying) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, videoDuration)
        exoPlayer.seekTo(target)
        currentPosition = target
        onSeeked(target / 1000)
    }

    fun selectSource(resolution: Resolution) {
        if (resolution != selectedSource) {
            preservePositionOnNextSourceChange = true
            selectedSource = resolution
            onTrackChanged(
                exoPlayer.currentPosition / 1000,
                resolution.quality,
                selectedSubtitles?.id ?: -1
            )
        }
    }

    fun selectSubtitles(subtitle: Subtitle?) {
        if (subtitle != selectedSubtitles) {
            selectedSubtitles = subtitle
            onTrackChanged(
                exoPlayer.currentPosition / 1000,
                selectedSource.quality,
                subtitle?.id ?: -1
            )
        }
    }

    fun onEpisodeDataUpdated(
        newResolutions: List<Resolution>,
        newSubtitles: List<Subtitle>
    ) {
        availableResolutions = newResolutions
        availableSubtitles = newSubtitles
        selectedSubtitles = newSubtitles.firstOrNull { it.default }

        val targetResolution = newResolutions.firstOrNull { it.quality == selectedSource.quality }
            ?: newResolutions.last()

        preservePositionOnNextSourceChange = false
        selectedSource = targetResolution
        loadMediaItem(targetResolution, newSubtitles, preservePosition = false)
    }

    @OptIn(UnstableApi::class)
    fun loadMediaItem(
        resolution: Resolution,
        subtitles: List<Subtitle>,
        preservePosition: Boolean
    ) {
        subtitleText = ""
        val subtitleConfigs = subtitles.map { subtitle ->
            MediaItem.SubtitleConfiguration.Builder(subtitle.src.toUri())
                .setMimeType(MimeTypes.TEXT_VTT)
                .setId(subtitle.src)
                .setLabel(subtitle.label)
                .build()
        }

        val mediaItem = MediaItem.Builder()
            .setUri(resolution.src)
            .setSubtitleConfigurations(subtitleConfigs)
            .build()

        setItem(mediaItem, preservePosition)
    }

    @OptIn(UnstableApi::class)
    fun setItem(mediaItem: MediaItem, preservePosition: Boolean) {
        val hasMedia = exoPlayer.mediaItemCount > 0
        val savedPosition = if (preservePosition && hasMedia) exoPlayer.currentPosition else 0L
        val pwr = if (hasMedia) exoPlayer.playWhenReady else playWhenReadyState

        if (!preservePosition) {
            currentPosition = 0L
            videoDuration = 1L
            hasStartedPlayback = false
            subtitleText = ""
        }

        exoPlayer.replaceMediaItem(0, mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(savedPosition)
        exoPlayer.playWhenReady = pwr
    }

    internal fun consumePreservePosition(): Boolean {
        val value = preservePositionOnNextSourceChange
        preservePositionOnNextSourceChange = false
        return value
    }
}

@OptIn(UnstableApi::class)
@Composable
fun rememberVideoPlayerState(
    dataSourceFactory: DataSource.Factory,
    availableResolutions: List<Resolution>,
    availableSubtitles: List<Subtitle>,
    episode: String,
    onResumed: (Long) -> Unit,
    onPaused: (Long) -> Unit,
    onSeeked: (Long) -> Unit,
    onTrackChanged: (Long, String, Int) -> Unit,
    playNextEpisode: () -> Unit,
    playPreviousEpisode: () -> Unit
): VideoPlayerState {
    val context = if (Build.VERSION.SDK_INT >= 30) {
        LocalContext.current.createAttributionContext("playback")
    } else {
        LocalContext.current
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    val fullDataSourceFactory = DefaultDataSource.Factory(context, dataSourceFactory)

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(fullDataSourceFactory))
            .build()
            .apply {
                playWhenReady = true
                addAnalyticsListener(EventLogger())
            }
    }

    val state = remember(exoPlayer) {
        VideoPlayerState(
            exoPlayer = exoPlayer,
            initialResolutions = availableResolutions,
            initialSubtitles = availableSubtitles,
            onResumed = onResumed,
            onPaused = onPaused,
            onSeeked = onSeeked,
            onTrackChanged = onTrackChanged,
            playNextEpisode = playNextEpisode,
            playPreviousEpisode = playPreviousEpisode
        )
    }

    LaunchedEffect(Unit) {
        state.loadMediaItem(
            state.selectedSource,
            state.availableSubtitles,
            preservePosition = false
        )
    }

    LaunchedEffect(availableResolutions, availableSubtitles, episode) {
        state.onEpisodeDataUpdated(availableResolutions, availableSubtitles)
    }

    LaunchedEffect(state.selectedSource) {
        if (state.consumePreservePosition())
            state.loadMediaItem(state.selectedSource, state.availableSubtitles, true)
    }

    LaunchedEffect(state.selectedSubtitles, state.currentTracks) {
        val trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
        val currentSub = state.selectedSubtitles
        if (currentSub == null) {
            trackSelectionParameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            trackSelectionParameters.clearOverridesOfType(C.TRACK_TYPE_TEXT)
        } else {
            trackSelectionParameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            state.currentTracks.groups.find { trackGroup ->
                trackGroup.mediaTrackGroup.getFormat(0).id?.split(":")?.drop(1)
                    ?.joinToString(":") == currentSub.src
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
                Log.d("VideoPlayerState", "Playback state changed: $playbackState")
                if (playbackState == Player.STATE_READY) {
                    state.videoDuration = exoPlayer.contentDuration.coerceAtLeast(1L)
                    state.hasStartedPlayback = true
                } else if (playbackState == Player.STATE_ENDED) {
                    state.playNextEpisode()
                }
            }

            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                state.isPlaying = isPlayingState
                if (state.hasStartedPlayback && exoPlayer.playbackState != Player.STATE_ENDED) {
                    if (isPlayingState) {
                        state.onResumed(exoPlayer.currentPosition / 1000)
                    } else {
                        state.onPaused(exoPlayer.currentPosition / 1000)
                    }
                }
            }

            override fun onCues(cueGroup: CueGroup) {
                state.subtitleText = cueGroup.cues.firstOrNull()?.text?.toString() ?: ""
            }

            override fun onTracksChanged(tracks: Tracks) {
                state.currentTracks = tracks
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    state.videoSizePx = Size(
                        width = videoSize.width.toFloat(),
                        height = videoSize.height.toFloat()
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoPlayerState", "Player error: ${error.errorCodeName}", error)
            }
        }
        exoPlayer.addListener(listener)
        state.currentTracks = exoPlayer.currentTracks
        state.isPlaying = exoPlayer.isPlaying
        if (exoPlayer.playbackState == Player.STATE_READY) {
            state.videoDuration = exoPlayer.contentDuration.coerceAtLeast(1L)
        }

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (state.playWhenReadyState) {
                        exoPlayer.play()
                    }
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.isPlaying) {
        while (true) {
            if (!state.isDraggingSlider) {
                state.currentPosition = exoPlayer.currentPosition
            }
            delay(1.seconds)
        }
    }

    return state
}
