package dev.daika.davy.feature.home

import dev.daika.davy.core.domain.model.Feed
import dev.daika.davy.core.domain.usecase.YummyGetFeedUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFeedUseCase: YummyGetFeedUseCase = mockk()

    @Test
    fun `init loads feed and sets Success state on successful fetch`() = runTest {
        val expectedFeed = Feed(season = 1, year = 2026, items = emptyList())
        coEvery { getFeedUseCase() } returns expectedFeed

        val viewModel = HomeScreenViewModel(getFeedUseCase)

        val state = viewModel.uiState.value
        assertTrue(state is HomeScreenUiState.Success)
        assertEquals(expectedFeed, (state as HomeScreenUiState.Success).feed)
    }

    @Test
    fun `init sets Error state when fetch throws exception`() = runTest {
        val errorMessage = "Network timeout"
        coEvery { getFeedUseCase() } throws RuntimeException(errorMessage)

        val viewModel = HomeScreenViewModel(getFeedUseCase)

        val state = viewModel.uiState.value
        assertTrue(state is HomeScreenUiState.Error)
        assertEquals(errorMessage, (state as HomeScreenUiState.Error).message)
    }

    @Test
    fun `loadFeed updates state to Success after a previous Error`() = runTest {
        coEvery { getFeedUseCase() } throws RuntimeException("Initial failure")
        val viewModel = HomeScreenViewModel(getFeedUseCase)
        assertTrue(viewModel.uiState.value is HomeScreenUiState.Error)

        val recoveredFeed = Feed(season = 2, year = 2026, items = emptyList())
        coEvery { getFeedUseCase() } returns recoveredFeed

        viewModel.loadFeed()

        val state = viewModel.uiState.value
        assertTrue(state is HomeScreenUiState.Success)
        assertEquals(recoveredFeed, (state as HomeScreenUiState.Success).feed)
    }
}
