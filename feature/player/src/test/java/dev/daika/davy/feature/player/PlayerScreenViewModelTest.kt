package dev.daika.davy.feature.player

import androidx.lifecycle.SavedStateHandle
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeEpisode
import dev.daika.davy.core.domain.model.AnimePlayer
import dev.daika.davy.core.domain.model.AnimeRating
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.domain.model.AnimeTranslation
import dev.daika.davy.core.domain.usecase.YummyGetAnimeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val yummyGetAnimeUseCase: YummyGetAnimeUseCase = mockk()
    private val okHttpClient: OkHttpClient = OkHttpClient()

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
        val anime = Anime(
            id = 1,
            title = "Test Anime",
            description = "Desc",
            url = "url",
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
                                AnimeEpisode(videoId = 10, title = "1", iframeUrl = "//player/10")
                            )
                        )
                    )
                )
            )
        )
        coEvery { yummyGetAnimeUseCase(1, true) } returns anime
        // Request episodeId = 999 which doesn't exist
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
}
