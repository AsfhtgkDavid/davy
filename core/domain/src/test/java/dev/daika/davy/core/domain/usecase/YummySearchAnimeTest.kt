package dev.daika.davy.core.domain.usecase

import androidx.paging.PagingData
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.repository.FakeAnimeRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class YummySearchAnimeTest {

    private lateinit var repository: FakeAnimeRepository
    private lateinit var useCase: YummySearchAnime

    @Before
    fun setup() {
        repository = FakeAnimeRepository()
        useCase = YummySearchAnime(repository)
    }

    @Test
    fun `invoke passes query and filter to repository and returns flow`() {
        val expectedFlow = flowOf(PagingData.empty<Anime>())
        repository.searchFlowToReturn = expectedFlow

        val filter = AnimeFilterState(selectedGenres = setOf("fantasy"))
        val resultFlow = useCase(query = "Naruto", filter = filter)

        assertEquals(expectedFlow, resultFlow)
        assertEquals("Naruto", repository.lastSearchQuery)
        assertEquals(filter, repository.lastSearchFilter)
    }
}
