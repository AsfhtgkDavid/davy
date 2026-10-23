package dev.daika.davy.feature.details

import androidx.lifecycle.SavedStateHandle
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeRating
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.domain.usecase.YummyGetAnimeUseCase
import dev.daika.davyparsers.Parser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeDetailsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAnimeUseCase: YummyGetAnimeUseCase = mockk()
    private val emptyParsers: List<Parser> = emptyList()

    private val sampleAnime = Anime(
        id = 123,
        title = "Frieren",
        description = "Beyond Journey's End",
        url = "frieren",
        poster = "poster.jpg",
        rating = AnimeRating(9.3, 1500),
        status = AnimeStatus.RELEASED,
        translations = emptyList()
    )

    @Test
    fun `init loads anime details and sets Success state`() = runTest {
        coEvery { getAnimeUseCase(123, true) } returns sampleAnime
        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 123))

        val viewModel = AnimeDetailsScreenViewModel(
            savedStateHandle = savedStateHandle,
            getAnimeUseCase = getAnimeUseCase,
            parsers = emptyParsers
        )

        val state = viewModel.uiState.value
        assertTrue(state is AnimeDetailsScreenUiState.Success)
        assertEquals(sampleAnime, (state as AnimeDetailsScreenUiState.Success).anime)
    }

    @Test
    fun `init sets Error state when usecase throws exception`() = runTest {
        val errorMessage = "Anime not found"
        coEvery { getAnimeUseCase(404, true) } throws RuntimeException(errorMessage)
        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 404))

        val viewModel = AnimeDetailsScreenViewModel(
            savedStateHandle = savedStateHandle,
            getAnimeUseCase = getAnimeUseCase,
            parsers = emptyParsers
        )

        val state = viewModel.uiState.value
        assertTrue(state is AnimeDetailsScreenUiState.Error)
        assertEquals(errorMessage, (state as AnimeDetailsScreenUiState.Error).message)
    }

    @Test
    fun `getAnimeDetails can reload after error`() = runTest {
        coEvery { getAnimeUseCase(123, true) } throws RuntimeException("First error")
        val savedStateHandle = SavedStateHandle(mapOf("animeId" to 123))

        val viewModel = AnimeDetailsScreenViewModel(
            savedStateHandle = savedStateHandle,
            getAnimeUseCase = getAnimeUseCase,
            parsers = emptyParsers
        )
        assertTrue(viewModel.uiState.value is AnimeDetailsScreenUiState.Error)

        coEvery { getAnimeUseCase(123, true) } returns sampleAnime
        viewModel.getAnimeDetails()

        val state = viewModel.uiState.value
        assertTrue(state is AnimeDetailsScreenUiState.Success)
        assertEquals(sampleAnime, (state as AnimeDetailsScreenUiState.Success).anime)
    }
}
