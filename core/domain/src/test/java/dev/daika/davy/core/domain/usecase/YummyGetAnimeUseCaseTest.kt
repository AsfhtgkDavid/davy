package dev.daika.davy.core.domain.usecase

import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeRating
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.domain.repository.FakeAnimeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class YummyGetAnimeUseCaseTest {

    private lateinit var repository: FakeAnimeRepository
    private lateinit var useCase: YummyGetAnimeUseCase

    @Before
    fun setup() {
        repository = FakeAnimeRepository()
        useCase = YummyGetAnimeUseCase(repository)
    }

    @Test
    fun `invoke with default needVideos passes false and requested id`() = runTest {
        val anime = Anime(
            id = 42,
            title = "Attack on Titan",
            description = "Desc",
            url = "aot",
            poster = "poster.jpg",
            rating = AnimeRating(9.0, 500),
            status = AnimeStatus.RELEASED
        )
        repository.animeDetailsToReturn = anime

        val result = useCase(id = 42)

        assertEquals(anime, result)
        assertEquals(42, repository.lastRequestedAnimeId)
        assertEquals(false, repository.lastNeedVideos)
    }

    @Test
    fun `invoke with needVideos true passes true and requested id`() = runTest {
        useCase(id = 100, needVideos = true)

        assertEquals(100, repository.lastRequestedAnimeId)
        assertEquals(true, repository.lastNeedVideos)
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke propagates repository exceptions`() = runTest {
        repository.shouldThrowError = true
        useCase(id = 1)
    }
}
