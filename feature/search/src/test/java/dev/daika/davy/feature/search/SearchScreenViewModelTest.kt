package dev.daika.davy.feature.search

import androidx.paging.PagingData
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.AnimeGenreFilter
import dev.daika.davy.core.domain.model.FilterOption
import dev.daika.davy.core.domain.usecase.YummyGetAnimeGenres
import dev.daika.davy.core.domain.usecase.YummySearchAnime
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val yummySearchAnime: YummySearchAnime = mockk()
    private val yummyGetAnimeGenres: YummyGetAnimeGenres = mockk()

    @Before
    fun setup() {
        every { yummySearchAnime(any(), any()) } returns flowOf(PagingData.empty<Anime>())
    }

    @Test
    fun `init loads genres and populates genreOptions`() = runTest {
        val genres = listOf(
            AnimeGenreFilter(id = "action", title = "Action"),
            AnimeGenreFilter(id = "comedy", title = "Comedy")
        )
        coEvery { yummyGetAnimeGenres() } returns genres

        val viewModel = SearchScreenViewModel(
            yummySearchAnime = yummySearchAnime,
            yummyGetAnimeGenres = yummyGetAnimeGenres
        )

        val expectedOptions = listOf(
            FilterOption(id = "action", title = "Action"),
            FilterOption(id = "comedy", title = "Comedy")
        )
        assertEquals(expectedOptions, viewModel.genreOptions.value)
    }

    @Test
    fun `updateSearchQuery updates searchQuery state`() = runTest {
        coEvery { yummyGetAnimeGenres() } returns emptyList()

        val viewModel = SearchScreenViewModel(
            yummySearchAnime = yummySearchAnime,
            yummyGetAnimeGenres = yummyGetAnimeGenres
        )

        assertEquals("", viewModel.searchQuery.value)

        viewModel.updateSearchQuery("Solo Leveling")
        assertEquals("Solo Leveling", viewModel.searchQuery.value)
    }

    @Test
    fun `updateFilterState updates filterState`() = runTest {
        coEvery { yummyGetAnimeGenres() } returns emptyList()

        val viewModel = SearchScreenViewModel(
            yummySearchAnime = yummySearchAnime,
            yummyGetAnimeGenres = yummyGetAnimeGenres
        )

        val newFilter = AnimeFilterState(
            selectedGenres = setOf("fantasy"),
            yearFrom = 2024
        )
        viewModel.updateFilterState(newFilter)

        assertEquals(newFilter, viewModel.filterState.value)
    }
}
