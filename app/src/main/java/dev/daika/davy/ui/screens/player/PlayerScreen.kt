package dev.daika.davy.ui.screens.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import kotlinx.serialization.Serializable

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(playerScreenViewModel: PlayerScreenViewModel = hiltViewModel()) {
    val uiState by playerScreenViewModel.uiState.collectAsState()

    Log.i("PlayerScreen", "PlayerScreen recomposed with uiState: $uiState")

    when (uiState) {
        is PlayerScreenUiState.Loading -> {
            // Show loading indicator
        }

        is PlayerScreenUiState.Success -> {
            val playerData = (uiState as PlayerScreenUiState.Success).playerData
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            var selectedSource by remember {
                mutableStateOf(playerData.translations.first().streams.first().urls.first())
            }
            var savedPosition by remember {
                mutableLongStateOf(0L)
            }
            var playWhenReadyState by remember {
                mutableStateOf(false)
            }

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(
                    mapOf(
                        "Origin" to (uiState as PlayerScreenUiState.Success).iframeUrl
                    )
                )
            val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
            val exoPlayer = remember {
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                    .build()
            }

            LaunchedEffect(selectedSource) {
                val mediaItem = MediaItem.fromUri(selectedSource)

                if (exoPlayer.mediaItemCount > 0) {
                    savedPosition = exoPlayer.currentPosition
                    playWhenReadyState = exoPlayer.playWhenReady
                }

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()

                exoPlayer.seekTo(savedPosition)
                exoPlayer.playWhenReady = playWhenReadyState
            }

            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
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

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = true
                        keepScreenOn = true
                    }
                }
            )
        }

        is PlayerScreenUiState.Error -> {
            // Show error message
        }
    }
}

@Serializable
data class PlayerScreenDestination(
    val animeId: Int,
    val episodeId: Int
)