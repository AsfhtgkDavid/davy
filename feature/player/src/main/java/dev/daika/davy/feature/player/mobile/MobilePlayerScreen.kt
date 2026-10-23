package dev.daika.davy.feature.player.mobile

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
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
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import dev.daika.davy.core.common.util.formatTime
import dev.daika.davy.core.ui.component.HideBars
import dev.daika.davy.feature.player.PlayerScreenUiState
import dev.daika.davy.feature.player.PlayerScreenViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobilePlayerScreen(
    onBackPressed: () -> Unit,
    playerScreenViewModel: PlayerScreenViewModel = hiltViewModel()
) {
    val uiState by playerScreenViewModel.uiState.collectAsState()

    when (val currentState = uiState) {
        is PlayerScreenUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is PlayerScreenUiState.Success -> {
            val playerData = currentState.playerData
            val animeTitle = currentState.animeTitle
            val episodeNumber = currentState.episodeNumber

            val context =
                if (Build.VERSION.SDK_INT >= 30) LocalContext.current.createAttributionContext("playback") else LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            var isControlsVisible by remember { mutableStateOf(true) }
            var isPlaying by remember { mutableStateOf(false) }

            var currentPosition by remember { mutableLongStateOf(0L) }
            var videoDuration by remember { mutableLongStateOf(1L) }
            var isDraggingSlider by remember { mutableStateOf(false) }
            var sliderProgress by remember { mutableFloatStateOf(0f) }

            var selectedSource by remember {
                mutableStateOf(
                    (playerData.translations.firstOrNull { it.isDefault }
                        ?: playerData.translations.first()).streams.maxByOrNull { it.quality.toInt() }!!.urls.first()
                )
            }
            var selectedSubtitles by remember {
                mutableStateOf(playerData.subtitles.firstOrNull { it.isDefault }?.src)
            }

            var playWhenReadyState by remember { mutableStateOf(true) }
            var subtitleText by remember { mutableStateOf("") }
            var currentTracks by remember { mutableStateOf(Tracks.EMPTY) }

            var showQualitySheet by remember { mutableStateOf(false) }
            var showSubtitleSheet by remember { mutableStateOf(false) }

            var videoSizePx by remember { mutableStateOf<Size?>(null) }

            val dataSourceFactory = DefaultDataSource.Factory(
                context,
                currentState.dataSourceFactory
            )
            val exoPlayer = remember {
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                    .build()
                    .apply {
                        playWhenReady = true
                    }
            }

            fun setItem(mediaItem: MediaItem) {
                var savedPosition1 = 0L
                var playWhenReady1 = playWhenReadyState
                if (exoPlayer.mediaItemCount > 0) {
                    savedPosition1 = exoPlayer.currentPosition
                    playWhenReady1 = exoPlayer.playWhenReady
                }

                exoPlayer.replaceMediaItem(0, mediaItem)
                exoPlayer.prepare()
                exoPlayer.seekTo(savedPosition1)
                exoPlayer.playWhenReady = playWhenReady1
            }

            BackHandler {
                onBackPressed()
            }

            var userInteractionCount by remember { mutableIntStateOf(0) }
            LaunchedEffect(isControlsVisible, isPlaying, userInteractionCount) {
                if (isControlsVisible && isPlaying && !isDraggingSlider) {
                    delay(4.seconds)
                    isControlsVisible = false
                }
            }

            LaunchedEffect(isPlaying, isControlsVisible) {
                while (true) {
                    if (!isDraggingSlider) {
                        currentPosition = exoPlayer.currentPosition
                    }
                    delay(5.seconds)
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

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        if (videoSize.width > 0 && videoSize.height > 0) {
                            videoSizePx = Size(
                                width = videoSize.width.toFloat(),
                                height = videoSize.height.toFloat()
                            )
                        }
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

            HideBars()

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
                key(selectedSource) {
                    PlayerSurface(
                        player = exoPlayer,
                        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                        modifier = Modifier
                            .fillMaxSize()
                            .resizeWithContentScale(
                                contentScale = ContentScale.Fit,
                                sourceSizeDp = videoSizePx
                            )
                    )
                }

                // Subtitle Display
                if (subtitleText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (isControlsVisible) 110.dp else 36.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = subtitleText,
                            color = Color.White,
                            fontSize = 18.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Touch Controls Overlay
                AnimatedVisibility(
                    visible = isControlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
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
                        // Top Bar: Back button, Anime Title & Episode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .align(Alignment.TopStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = animeTitle,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Episode $episodeNumber",
                                    color = Color.White.copy(alpha = 0.75f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Center Controls: Prev, Rewind 10s, Play/Pause, Forward 10s, Next
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    userInteractionCount++
                                    playerScreenViewModel.playPreviousEpisode()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous episode",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    userInteractionCount++
                                    val newPos =
                                        (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "-10 sec",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Big Play / Pause Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable {
                                        userInteractionCount++
                                        if (isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.play()
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    userInteractionCount++
                                    val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(
                                        videoDuration
                                    )
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "+10 sec",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    userInteractionCount++
                                    playerScreenViewModel.playNextEpisode()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next episode",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Bottom Bar: Slider + Times + Quality/Subtitles
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            // Slider
                            val safeTotal = videoDuration.coerceAtLeast(1L).toFloat()
                            val progressValue = if (isDraggingSlider) {
                                sliderProgress
                            } else {
                                (currentPosition.toFloat() / safeTotal).coerceIn(0f, 1f)
                            }

                            Slider(
                                value = progressValue,
                                onValueChange = {
                                    isDraggingSlider = true
                                    sliderProgress = it
                                    userInteractionCount++
                                },
                                onValueChangeFinished = {
                                    val targetPosition = (sliderProgress * safeTotal).toLong()
                                    exoPlayer.seekTo(targetPosition)
                                    currentPosition = targetPosition
                                    isDraggingSlider = false
                                    userInteractionCount++
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Time and settings actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currentDisplay = if (isDraggingSlider) {
                                    (sliderProgress * safeTotal).toLong()
                                } else {
                                    currentPosition
                                }

                                Text(
                                    text = "${formatTime(currentDisplay)} / ${
                                        formatTime(
                                            videoDuration
                                        )
                                    }",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Quality Button
                                    IconButton(
                                        onClick = {
                                            userInteractionCount++
                                            showQualitySheet = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = "Quality",
                                            tint = Color.White
                                        )
                                    }

                                    // Subtitles Button
                                    if (playerData.subtitles.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                userInteractionCount++
                                                showSubtitleSheet = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Subtitles,
                                                contentDescription = "Subtitles",
                                                tint = if (selectedSubtitles != null) MaterialTheme.colorScheme.primary else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quality Bottom Sheet
                if (showQualitySheet) {
                    val streams = (playerData.translations.firstOrNull { it.isDefault }
                        ?: playerData.translations.first()).streams.sortedBy { it.quality.toInt() }

                    ModalBottomSheet(
                        onDismissRequest = { showQualitySheet = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Select video quality",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            streams.forEach { stream ->
                                val isSelected = stream.urls.contains(selectedSource)
                                Surface(
                                    onClick = {
                                        selectedSource = stream.urls.first()
                                        showQualitySheet = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ) {
                                    Text(
                                        text = "${stream.quality}p",
                                        modifier = Modifier.padding(14.dp),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Subtitles Bottom Sheet
                if (showSubtitleSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSubtitleSheet = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Subtitles",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Option: Turn Off
                            Surface(
                                onClick = {
                                    selectedSubtitles = null
                                    showSubtitleSheet = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedSubtitles == null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ) {
                                Text(
                                    text = "Off",
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (selectedSubtitles == null) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedSubtitles == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // All available subtitles
                            playerData.subtitles.forEach { subtitle ->
                                val isSelected = subtitle.src == selectedSubtitles
                                Surface(
                                    onClick = {
                                        selectedSubtitles = subtitle.src
                                        showSubtitleSheet = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ) {
                                    Text(
                                        text = subtitle.label,
                                        modifier = Modifier.padding(14.dp),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        is PlayerScreenUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Playback error",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

