package dev.daika.davy.feature.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dev.daika.davy.feature.player.state.VideoPlayerState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VideoPlayerStateTest {

    private lateinit var exoPlayer: ExoPlayer

    private val res360 = Resolution(quality = "360p", src = "https://cdn.example.com/360.m3u8")
    private val res720 = Resolution(quality = "720p", src = "https://cdn.example.com/720.m3u8")
    private val res1080 = Resolution(quality = "1080p", src = "https://cdn.example.com/1080.m3u8")

    private val subEn =
        Subtitle(id = 1, label = "English", src = "https://cdn.example.com/en.vtt", default = false)
    private val subUa = Subtitle(
        id = 2,
        label = "Ukrainian",
        src = "https://cdn.example.com/ua.vtt",
        default = true
    )

    private var resumedAt: Long? = null
    private var pausedAt: Long? = null
    private var seekedTo: Long? = null
    private var trackChangedArgs: Triple<Long, String, Int>? = null
    private var nextEpisodeCalled = false
    private var prevEpisodeCalled = false

    @Before
    fun setUp() {
        exoPlayer = mockk(relaxed = true) {
            every { mediaItemCount } returns 1
            every { currentPosition } returns 45_000L
            every { playWhenReady } returns true
        }
        resumedAt = null
        pausedAt = null
        seekedTo = null
        trackChangedArgs = null
        nextEpisodeCalled = false
        prevEpisodeCalled = false
    }

    private fun createState(
        resolutions: List<Resolution> = listOf(res360, res720, res1080),
        subtitles: List<Subtitle> = listOf(subEn, subUa)
    ): VideoPlayerState = VideoPlayerState(
        exoPlayer = exoPlayer,
        initialResolutions = resolutions,
        initialSubtitles = subtitles,
        onResumed = { resumedAt = it },
        onPaused = { pausedAt = it },
        onSeeked = { seekedTo = it },
        onTrackChanged = { time, res, subId -> trackChangedArgs = Triple(time, res, subId) },
        playNextEpisode = { nextEpisodeCalled = true },
        playPreviousEpisode = { prevEpisodeCalled = true }
    )

    @Test
    fun `initial state selects highest resolution and default subtitle`() {
        val state = createState()

        assertEquals(res1080, state.selectedSource)
        assertEquals(subUa, state.selectedSubtitles)
        assertTrue(state.playWhenReadyState)
        assertFalse(state.hasStartedPlayback)
    }

    @Test
    fun `play and pause update playWhenReadyState and delegate to exoPlayer`() {
        val state = createState()

        state.pause()
        assertFalse(state.playWhenReadyState)
        verify(exactly = 1) { exoPlayer.pause() }

        state.play()
        assertTrue(state.playWhenReadyState)
        verify(exactly = 1) { exoPlayer.play() }
    }

    @Test
    fun `togglePlayPause switches state based on isPlaying`() {
        val state = createState()

        state.isPlaying = true
        state.togglePlayPause()
        assertFalse(state.playWhenReadyState)
        verify(exactly = 1) { exoPlayer.pause() }

        state.isPlaying = false
        state.togglePlayPause()
        assertTrue(state.playWhenReadyState)
        verify(exactly = 1) { exoPlayer.play() }
    }

    @Test
    fun `seekTo clamps position within bounds and triggers callback in seconds`() {
        val state = createState().apply {
            videoDuration = 120_000L
        }

        state.seekTo(150_000L)
        assertEquals(120_000L, state.currentPosition)
        assertEquals(120L, seekedTo)
        verify { exoPlayer.seekTo(120_000L) }

        state.seekTo(-5_000L)
        assertEquals(0L, state.currentPosition)
        assertEquals(0L, seekedTo)
        verify { exoPlayer.seekTo(0L) }
    }

    @Test
    fun `selectSource sets preservePosition flag and notifies onTrackChanged`() {
        val state = createState()

        state.selectSource(res720)

        assertEquals(res720, state.selectedSource)
        assertEquals(Triple(45L, "720p", 2), trackChangedArgs)
        assertTrue(state.consumePreservePosition())
        assertFalse(state.consumePreservePosition())
    }

    @Test
    fun `selectSource ignores identical resolution`() {
        val state = createState()

        state.selectSource(res1080)

        assertNull(trackChangedArgs)
        assertFalse(state.consumePreservePosition())
    }

    @Test
    fun `selectSubtitles updates selection and notifies onTrackChanged`() {
        val state = createState()

        state.selectSubtitles(subEn)
        assertEquals(subEn, state.selectedSubtitles)
        assertEquals(Triple(45L, "1080p", 1), trackChangedArgs)

        state.selectSubtitles(null)
        assertNull(state.selectedSubtitles)
        assertEquals(Triple(45L, "1080p", -1), trackChangedArgs)
    }

    @Test
    fun `setItem preserves position when requested`() {
        val state = createState()
        val mediaItem = MediaItem.fromUri(res720.src)

        state.setItem(mediaItem, preservePosition = true)

        verifyOrder {
            exoPlayer.replaceMediaItem(0, mediaItem)
            exoPlayer.prepare()
            exoPlayer.seekTo(45_000L)
            exoPlayer.playWhenReady = true
        }
    }

    @Test
    fun `setItem resets state when preservePosition is false`() {
        val state = createState().apply {
            currentPosition = 45_000L
            videoDuration = 120_000L
            hasStartedPlayback = true
            subtitleText = "Привет"
        }
        val mediaItem = MediaItem.fromUri(res1080.src)

        state.setItem(mediaItem, preservePosition = false)

        assertEquals(0L, state.currentPosition)
        assertEquals(1L, state.videoDuration)
        assertFalse(state.hasStartedPlayback)
        assertEquals("", state.subtitleText)
        verify { exoPlayer.seekTo(0L) }
    }

    @Test
    fun `onEpisodeDataUpdated keeps matching quality and resets playback state`() {
        val state = createState().apply {
            selectSource(res720)
            currentPosition = 30_000L
            hasStartedPlayback = true
            subtitleText = "Old text"
        }

        val ep2Res360 = Resolution(quality = "360p", src = "https://cdn.example.com/ep2_360.m3u8")
        val ep2Res720 = Resolution(quality = "720p", src = "https://cdn.example.com/ep2_720.m3u8")
        val ep2Res1080 =
            Resolution(quality = "1080p", src = "https://cdn.example.com/ep2_1080.m3u8")
        val ep2Sub = Subtitle(
            id = 10,
            label = "English",
            src = "https://cdn.example.com/ep2_en.vtt",
            default = true
        )

        state.onEpisodeDataUpdated(
            newResolutions = listOf(ep2Res360, ep2Res720, ep2Res1080),
            newSubtitles = listOf(ep2Sub)
        )

        assertEquals(ep2Res720, state.selectedSource)
        assertEquals(ep2Sub, state.selectedSubtitles)
        assertEquals(0L, state.currentPosition)
        assertFalse(state.hasStartedPlayback)
        assertFalse(state.consumePreservePosition())
        verify { exoPlayer.seekTo(0L) }
    }

    @Test
    fun `onEpisodeDataUpdated falls back to highest resolution when previous quality is unavailable`() {
        val state = createState().apply {
            selectSource(res720)
        }

        val ep2Res480 = Resolution(quality = "480p", src = "https://cdn.example.com/ep2_480.m3u8")
        val ep2Res1080 =
            Resolution(quality = "1080p", src = "https://cdn.example.com/ep2_1080.m3u8")

        state.onEpisodeDataUpdated(
            newResolutions = listOf(ep2Res480, ep2Res1080),
            newSubtitles = emptyList()
        )

        assertEquals(ep2Res1080, state.selectedSource)
        assertNull(state.selectedSubtitles)
    }
}