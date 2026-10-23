package dev.daika.davy.core.data.repository

import androidx.paging.PagingSource
import dev.daika.davy.core.data.api.FakeYummyApi
import dev.daika.davy.core.data.model.AnimeGenreDto
import dev.daika.davy.core.data.model.AnimeStatusDto
import dev.daika.davy.core.data.model.SearchAnimeDto
import dev.daika.davy.core.domain.model.AnimeFilterState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class YummySearchPagingSourceTest {

    private lateinit var fakeApi: FakeYummyApi

    @Before
    fun setup() {
        fakeApi = FakeYummyApi()
    }

    private fun createSearchDto(id: Int, title: String) = SearchAnimeDto(
        id = id,
        title = title,
        description = "Description $id",
        url = "url-$id",
        poster = fakeApi.defaultPosters,
        rating = fakeApi.defaultRating,
        genres = listOf(AnimeGenreDto(1, "Action")),
        status = AnimeStatusDto(1)
    )

    @Test
    fun `load returns empty page when query is blank and no active filters`() = runTest {
        val pagingSource = YummySearchPagingSource(
            yummyApi = fakeApi,
            query = "   ",
            filter = AnimeFilterState()
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `load returns page with nextKey when full page of results is returned`() = runTest {
        val pagingSource = YummySearchPagingSource(
            yummyApi = fakeApi,
            query = "Bleach",
            filter = AnimeFilterState()
        )

        val items = (1..20).map { createSearchDto(it, "Bleach $it") }
        fakeApi.searchResultsToReturn = items

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(20, page.data.size)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
        assertEquals("Bleach", fakeApi.lastSearchQuery)
        assertEquals(0, fakeApi.lastSearchOffset)
        assertEquals(20, fakeApi.lastSearchLimit)
    }

    @Test
    fun `load on page 2 sets prevKey and null nextKey when results are fewer than loadSize`() = runTest {
        val pagingSource = YummySearchPagingSource(
            yummyApi = fakeApi,
            query = "Bleach",
            filter = AnimeFilterState()
        )

        val items = (1..5).map { createSearchDto(it, "Bleach $it") }
        fakeApi.searchResultsToReturn = items

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(5, page.data.size)
        assertEquals(1, page.prevKey)
        assertNull(page.nextKey)
        assertEquals(20, fakeApi.lastSearchOffset)
    }

    @Test
    fun `load returns Error when api throws exception`() = runTest {
        val pagingSource = YummySearchPagingSource(
            yummyApi = fakeApi,
            query = "Bleach",
            filter = AnimeFilterState()
        )

        val exception = RuntimeException("Server unavailable")
        fakeApi.shouldThrowError = true
        fakeApi.errorToThrow = exception

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        val errorResult = result as PagingSource.LoadResult.Error
        assertEquals(exception, errorResult.throwable)
    }
}
