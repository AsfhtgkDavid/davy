package dev.daika.davy.feature.player

import androidx.lifecycle.SavedStateHandle
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeEpisode
import dev.daika.davy.core.domain.model.AnimePlayer
import dev.daika.davy.core.domain.model.AnimeRating
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.domain.model.AnimeTranslation
import dev.daika.davy.core.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davyparsers.MediaStream
import dev.daika.davyparsers.Parser
import dev.daika.davyparsers.PlayerData
import dev.daika.davyparsers.SubtitleTrack
import dev.daika.davyparsers.Translation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
}