package dev.daika.davy.core.data.repository

import android.content.Context
import dev.daika.davy.core.data.api.FakeYummyApi
import dev.daika.davy.core.data.model.AnimeVideoDto
import dev.daika.davy.core.data.model.PlayerDataDto
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class YummyRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var fakeApi: FakeYummyApi
    private lateinit var repository: YummyRepository

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        every { mockContext.cacheDir } returns tempFolder.root
        fakeApi = FakeYummyApi()
        repository = YummyRepository(mockContext, fakeApi)
    }

    @Test
    fun `getFeed returns mapped entity from api`() = runTest {
        val feed = repository.getFeed()
        assertEquals(1, feed.season)
        assertEquals(2026, feed.year)
    }

    @Test
    fun `getAnimeDetails caches result on first call and uses cache on second call`() = runTest {
        assertEquals(0, fakeApi.getAnimeDetailsCallCount)

        // First call - should hit API
        val firstAnime = repository.getAnimeDetails(id = 10, needVideos = false)
        assertEquals(10, firstAnime.id)
        assertEquals(1, fakeApi.getAnimeDetailsCallCount)

        // Second call with same id - should hit in-memory cache, not API
        val secondAnime = repository.getAnimeDetails(id = 10, needVideos = false)
        assertEquals(10, secondAnime.id)
        assertEquals(1, fakeApi.getAnimeDetailsCallCount)
    }

    @Test
    fun `getAnimeDetails with needVideos true fetches videos when cached has no translations`() =
        runTest {
            fakeApi.videosToReturn = listOf(
                AnimeVideoDto(
                    id = 50,
                    iframeUrl = "//player/50",
                    data = PlayerDataDto(player = "Kodik", dubbing = "AniLibria"),
                    number = "1"
                )
            )

            // Cache anime without videos
            repository.getAnimeDetails(id = 20, needVideos = false)
            assertEquals(1, fakeApi.getAnimeDetailsCallCount)
            assertEquals(0, fakeApi.getAnimeVideosCallCount)

            // Now request with needVideos = true
            val animeWithVideos = repository.getAnimeDetails(id = 20, needVideos = true)
            assertEquals(1, fakeApi.getAnimeDetailsCallCount)
            assertEquals(1, fakeApi.getAnimeVideosCallCount)
            assertEquals(1, animeWithVideos.translations.size)
            assertEquals("AniLibria", animeWithVideos.translations[0].title)

            // Call again with needVideos = true - should be cached now, no extra video API calls
            val cachedWithVideos = repository.getAnimeDetails(id = 20, needVideos = true)
            assertEquals(1, fakeApi.getAnimeDetailsCallCount)
            assertEquals(1, fakeApi.getAnimeVideosCallCount)
            assertEquals(1, cachedWithVideos.translations.size)
        }

    @Test
    fun `getAnimeGenres writes to disk cache and reads from disk cache on second call`() = runTest {
        val genresFirst = repository.getAnimeGenres()
        assertEquals(1, genresFirst.size)
        assertEquals("Action", genresFirst[0].title)

        // Update API return value to ensure second call reads from disk cache
        fakeApi.genresResponseToReturn = fakeApi.genresResponseToReturn.copy(genres = emptyList())

        val genresSecond = repository.getAnimeGenres()
        assertEquals(1, genresSecond.size)
        assertEquals("Action", genresSecond[0].title)
    }
}
