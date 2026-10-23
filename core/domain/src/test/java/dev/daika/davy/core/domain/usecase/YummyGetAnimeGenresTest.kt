package dev.daika.davy.core.domain.usecase

import dev.daika.davy.core.domain.model.AnimeGenreFilter
import dev.daika.davy.core.domain.repository.FakeAnimeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class YummyGetAnimeGenresTest {

    private lateinit var repository: FakeAnimeRepository
    private lateinit var useCase: YummyGetAnimeGenres

    @Before
    fun setup() {
        repository = FakeAnimeRepository()
        useCase = YummyGetAnimeGenres(repository)
    }

    @Test
    fun `invoke calls repository getAnimeGenres and returns list`() = runTest {
        val genres = listOf(
            AnimeGenreFilter(id = "action", title = "Action"),
            AnimeGenreFilter(id = "comedy", title = "Comedy")
        )
        repository.genresToReturn = genres

        val actual = useCase()

        assertEquals(genres, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke propagates repository exceptions`() = runTest {
        repository.shouldThrowError = true
        useCase()
    }
}
