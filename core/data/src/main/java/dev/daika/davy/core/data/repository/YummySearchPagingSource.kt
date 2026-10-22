package dev.daika.davy.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.daika.davy.core.data.api.YummyApi
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState

class YummySearchPagingSource(
    private val yummyApi: YummyApi,
    private val query: String,
    private val filter: AnimeFilterState
) : PagingSource<Int, Anime>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Anime> {
        if (query.isBlank() && !filter.hasActiveFilters())
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        val page = params.key ?: 1
        return try {
            val response = yummyApi.searchAnime(
                query = query,
                selectedGenres = filter.selectedGenres,
                excludedGenres = filter.excludedGenres,
                selectedTypes = filter.selectedTypes,
                selectedStatuses = filter.selectedStatuses,
                yearFrom = filter.yearFrom,
                yearTo = filter.yearTo,
                selectedSort = filter.selectedSort?.takeIf { it != "none" },
                offset = (page - 1) * params.loadSize,
                limit = params.loadSize
            ).map { it.toEntity() }
            LoadResult.Page(
                data = response,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.isEmpty() || response.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Anime>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
