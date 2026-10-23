package dev.daika.davy.core.domain.usecase

import dev.daika.davy.core.domain.model.Feed
import dev.daika.davy.core.domain.repository.FakeAnimeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class YummyGetFeedUseCaseTest {

    private lateinit var repository: FakeAnimeRepository
    private lateinit var useCase: YummyGetFeedUseCase

    @Before
    fun setup() {
        repository = FakeAnimeRepository()
        useCase = YummyGetFeedUseCase(repository)
    }

    @Test
    fun `invoke calls repository getFeed and returns result`() = runTest {
        val expectedFeed = Feed(season = 2, year = 2026, items = emptyList())
        repository.feedToReturn = expectedFeed

        val actualFeed = useCase()

        assertEquals(expectedFeed, actualFeed)
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke propagates repository exceptions`() = runTest {
        repository.shouldThrowError = true
        useCase()
    }
}
