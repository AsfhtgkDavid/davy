package dev.daika.davy.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeEpisode
import dev.daika.davy.core.domain.model.AnimePlayer
import dev.daika.davy.core.domain.model.AnimeRating
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.domain.model.AnimeTranslation
import dev.daika.davy.core.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davyparsers.MediaStream
import dev.daika.davyparsers.Parser
import dev.daika.davyparsers.ParserEvent
import dev.daika.davyparsers.PlayerData
import dev.daika.davyparsers.SubtitleTrack
import dev.daika.davyparsers.Translation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerScreenViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val yummyGetAnimeUseCase: YummyGetAnimeUseCase = mockk()
    private val okHttpClient: OkHttpClient = OkHttpClient()

    private val testAnime = Anime(
        id = 1,
        title = "Test Anime",
        description = "Desc",
        url = "test-anime",
        poster = "poster.jpg",
        rating = AnimeRating(8.0, 100),
        status = AnimeStatus.RELEASED,
        translations = listOf(
            AnimeTranslation(
                title = "Dub",
                availablePlayers = listOf(
                    AnimePlayer(
                        player = "Kodik",
                        episodes = listOf(
                            AnimeEpisode(
                                videoId = 10,
                                title = "Episode 1",
                                iframeUrl = "//player/10"
                            ),
                            AnimeEpisode(
                                videoId = 20,
                                title = "Episode 2",
                                iframeUrl = "//player/20"
                            ),
                            AnimeEpisode(
                                videoId = 30,
                                title = "Episode 3",
                                iframeUrl = "//player/30"
                            )
                        )
                    )
                )
            )
        )
    )

    private val dummyPlayerData = PlayerData(
        headers = mapOf("User-Agent" to "DavyApp"),
        translations = listOf(
            Translation(
                id = "Default",
                name = "Default",
                isDefault = true,
                streams = listOf(
                    MediaStream(
                        quality = "1080p",
                        urls = listOf("https://cdn.example.com/video/1080/index.m3u8")
                    )
                )
            )
        ),
        subtitles = listOf(
            SubtitleTrack(
                label = "Mock",
                src = "https://cdn.example.com/subs/mk.vtt",
                isDefault = true,
                kind = "subtitle",
                language = "mk"
            )
        )
    )

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init sets Error state when anime is not found`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } throws RuntimeException("Anime not found")
        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10))

        val viewModel = PlayerScreenViewModel(
            savedStateHandle = savedStateHandle,
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = emptyList(),
            okHttpClient = okHttpClient
        )

        val state = viewModel.uiState.value
        assertTrue(state is PlayerScreenUiState.Error)
        assertEquals("Anime not found", (state as PlayerScreenUiState.Error).message)
    }

    @Test
    fun `init sets Error state when episode is not found in anime translations`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime
        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 999))

        val viewModel = PlayerScreenViewModel(
            savedStateHandle = savedStateHandle,
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = emptyList(),
            okHttpClient = okHttpClient
        )

        val state = viewModel.uiState.value
        assertTrue(state is PlayerScreenUiState.Error)
        assertEquals("Episode not found", (state as PlayerScreenUiState.Error).message)
    }

    @Test
    fun `init sets Error state when no parser is found for iframeUrl`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime
        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10))

        val viewModel = PlayerScreenViewModel(
            savedStateHandle = savedStateHandle,
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = emptyList(),
            okHttpClient = okHttpClient
        )

        val state = viewModel.uiState.value
        assertTrue(state is PlayerScreenUiState.Error)
        assertTrue((state as PlayerScreenUiState.Error).message.contains("No parser found"))
    }

    @Test
    fun `init successfully loads episode and sets Success state`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

        val mockParser = mockk<Parser>(relaxed = true)
        mockkObject(Parser)
        every { Parser.getParserForUrl("https://player/10", any()) } returns mockParser
        coEvery { mockParser.parse(eq("https://player/10"), any(), any()) } returns dummyPlayerData

        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10))

        val viewModel = PlayerScreenViewModel(
            savedStateHandle = savedStateHandle,
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = listOf(mockParser),
            okHttpClient = okHttpClient
        )

        val state = viewModel.uiState.value
        assertTrue(state is PlayerScreenUiState.Success)
        val successState = state as PlayerScreenUiState.Success

        assertEquals("Test Anime", successState.animeTitle)
        assertEquals("Episode 1", successState.episodeNumber)
        assertEquals(1, successState.availableResolutions.size)
        assertEquals("1080p", successState.availableResolutions.first().quality)
        assertEquals(
            "https://davy/video/1080p/index.m3u8",
            successState.availableResolutions.first().src
        )
        assertEquals(1, successState.availableSubtitles.size)
        assertEquals("Mock", successState.availableSubtitles.first().label)
    }

    @Test
    fun `playNextEpisode advances to next available episode and closes previous parser`() =
        runTest {
            coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

            val parser1 = mockk<Parser>(relaxed = true)
            val parser2 = mockk<Parser>(relaxed = true)

            mockkObject(Parser)
            every { Parser.getParserForUrl("https://player/10", any()) } returns parser1
            every { Parser.getParserForUrl("https://player/20", any()) } returns parser2
            coEvery { parser1.parse(any(), any(), any()) } returns dummyPlayerData
            coEvery { parser2.parse(any(), any(), any()) } returns dummyPlayerData

            val savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10))

            val viewModel = PlayerScreenViewModel(
                savedStateHandle = savedStateHandle,
                yummyGetAnimeUseCase = yummyGetAnimeUseCase,
                parsers = listOf(parser1, parser2),
                okHttpClient = okHttpClient
            )

            assertEquals(
                "Episode 1",
                (viewModel.uiState.value as PlayerScreenUiState.Success).episodeNumber
            )

            viewModel.playNextEpisode()

            val state = viewModel.uiState.value
            assertTrue(state is PlayerScreenUiState.Success)
            assertEquals("Episode 2", (state as PlayerScreenUiState.Success).episodeNumber)
            verify(exactly = 1) { parser1.closeSession() }
        }

    @Test
    fun `playPreviousEpisode switches back to previous episode`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

        val parser1 = mockk<Parser>(relaxed = true)
        val parser2 = mockk<Parser>(relaxed = true)

        mockkObject(Parser)
        every { Parser.getParserForUrl("https://player/20", any()) } returns parser2
        every { Parser.getParserForUrl("https://player/10", any()) } returns parser1
        coEvery { parser1.parse(any(), any(), any()) } returns dummyPlayerData
        coEvery { parser2.parse(any(), any(), any()) } returns dummyPlayerData

        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 20))

        val viewModel = PlayerScreenViewModel(
            savedStateHandle = savedStateHandle,
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = listOf(parser1, parser2),
            okHttpClient = okHttpClient
        )

        assertEquals(
            "Episode 2",
            (viewModel.uiState.value as PlayerScreenUiState.Success).episodeNumber
        )

        viewModel.playPreviousEpisode()

        val state = viewModel.uiState.value
        assertTrue(state is PlayerScreenUiState.Success)
        assertEquals("Episode 1", (state as PlayerScreenUiState.Success).episodeNumber)
        verify(exactly = 1) { parser2.closeSession() }
    }

    @Test
    fun `playback lifecycle events dispatch corresponding ParserEvents to current parser`() =
        runTest {
            coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

            val mockParser = mockk<Parser>(relaxed = true)
            mockkObject(Parser)
            every { Parser.getParserForUrl("https://player/10", any()) } returns mockParser
            coEvery { mockParser.parse(any(), any(), any()) } returns dummyPlayerData

            val viewModel = PlayerScreenViewModel(
                savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10)),
                yummyGetAnimeUseCase = yummyGetAnimeUseCase,
                parsers = listOf(mockParser),
                okHttpClient = okHttpClient
            )

            viewModel.onResumed(1500L)
            viewModel.onPaused(3000L)
            viewModel.onSeeked(5000L)
            viewModel.onTrackChanged(currentTime = 5500L, resolution = "1080", subtitleId = 0)

            verify(exactly = 1) { mockParser.sendEvent(ParserEvent.RESUMED(1500L)) }
            verify(exactly = 1) { mockParser.sendEvent(ParserEvent.PAUSED(3000L)) }
            verify(exactly = 1) { mockParser.sendEvent(ParserEvent.SEEKED(5000L)) }
            verify(exactly = 1) {
                mockParser.sendEvent(
                    ParserEvent.TRACK_CHANGED(
                        currentTime = 5500L,
                        resolution = "1080",
                        audioId = "Default",
                        subtitleId = 0
                    )
                )
            }
        }

    @Test
    fun `sorts available resolutions ascending and selects default translation audioId`() =
        runTest {
            coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

            val multiQualityData = PlayerData(
                headers = emptyMap(),
                translations = listOf(
                    Translation(
                        id = "101",
                        name = "Secondary",
                        isDefault = false,
                        streams = listOf(
                            MediaStream(listOf("https://cdn.example.com/480.m3u8"), "480")
                        )
                    ),
                    Translation(
                        id = "202",
                        name = "Main Dub",
                        isDefault = true,
                        streams = listOf(
                            MediaStream(listOf("https://cdn.example.com/1080.m3u8"), "1080"),
                            MediaStream(listOf("https://cdn.example.com/360.m3u8"), "360"),
                            MediaStream(listOf("https://cdn.example.com/720.m3u8"), "720")
                        )
                    )
                ),
                subtitles = listOf(
                    SubtitleTrack(
                        label = "Russian Full",
                        src = "https://cdn.example.com/subs/ru.vtt",
                        isDefault = true,
                        kind = "subtitle",
                        language = "ru"
                    )
                )
            )

            val mockParser = mockk<Parser>(relaxed = true)
            mockkObject(Parser)
            every { Parser.getParserForUrl("https://player/10", any()) } returns mockParser
            coEvery { mockParser.parse(any(), any(), any()) } returns multiQualityData

            val viewModel = PlayerScreenViewModel(
                savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10)),
                yummyGetAnimeUseCase = yummyGetAnimeUseCase,
                parsers = listOf(mockParser),
                okHttpClient = okHttpClient
            )

            val state = viewModel.uiState.value as PlayerScreenUiState.Success
            assertEquals(
                listOf("360", "720", "1080"),
                state.availableResolutions.map { it.quality })
            assertEquals(
                "https://davy/sub/Russian%20Full/ru.vtt",
                state.availableSubtitles.first().src
            )

            viewModel.onTrackChanged(1000L, "720", -1)
            verify(exactly = 1) {
                mockParser.sendEvent(
                    ParserEvent.TRACK_CHANGED(
                        currentTime = 1000L,
                        resolution = "720",
                        audioId = "202",
                        subtitleId = -1
                    )
                )
            }
        }

    @Test
    fun `playNextEpisode and playPreviousEpisode do nothing on boundary episodes`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

        val mockParser = mockk<Parser>(relaxed = true)
        mockkObject(Parser)
        every { Parser.getParserForUrl(any(), any()) } returns mockParser
        coEvery { mockParser.parse(any(), any(), any()) } returns dummyPlayerData

        val firstEpViewModel = PlayerScreenViewModel(
            savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10)),
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = listOf(mockParser),
            okHttpClient = okHttpClient
        )
        firstEpViewModel.playPreviousEpisode()
        verify(exactly = 0) { mockParser.closeSession() }

        val lastEpViewModel = PlayerScreenViewModel(
            savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 30)),
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = listOf(mockParser),
            okHttpClient = okHttpClient
        )
        lastEpViewModel.playNextEpisode()
        verify(exactly = 0) { mockParser.closeSession() }
    }

    @Test
    fun `closes current parser session when parse fails or ViewModel is cleared`() = runTest {
        coEvery { yummyGetAnimeUseCase(1, true) } returns testAnime

        val failingParser = mockk<Parser>(relaxed = true)
        mockkObject(Parser)
        every { Parser.getParserForUrl("https://player/10", any()) } returns failingParser
        coEvery { failingParser.parse(any(), any(), any()) } throws RuntimeException("Parse failed")

        val failedViewModel = PlayerScreenViewModel(
            savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 10)),
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = listOf(failingParser),
            okHttpClient = okHttpClient
        )

        assertTrue(failedViewModel.uiState.value is PlayerScreenUiState.Error)
        verify(exactly = 1) { failingParser.closeSession() }

        val workingParser = mockk<Parser>(relaxed = true)
        every { Parser.getParserForUrl("https://player/20", any()) } returns workingParser
        coEvery { workingParser.parse(any(), any(), any()) } returns dummyPlayerData

        val activeViewModel = PlayerScreenViewModel(
            savedStateHandle = SavedStateHandle(mapOf("animeId" to 1, "episodeId" to 20)),
            yummyGetAnimeUseCase = yummyGetAnimeUseCase,
            parsers = listOf(workingParser),
            okHttpClient = okHttpClient
        )

        val store = ViewModelStore()
        store.put("PlayerScreenViewModel", activeViewModel)
        store.clear()

        verify(exactly = 1) { workingParser.closeSession() }
    }
}