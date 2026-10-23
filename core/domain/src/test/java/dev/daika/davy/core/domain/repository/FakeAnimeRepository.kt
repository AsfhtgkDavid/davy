package dev.daika.davy.core.domain.repository

import androidx.paging.PagingData
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.AnimeGenreFilter
import dev.daika.davy.core.domain.model.AnimeRating
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.domain.model.Feed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeAnimeRepository : AnimeRepository {

    var feedToReturn: Feed = Feed(season = 1, year = 2026, items = emptyList())
    var animeDetailsToReturn: Anime = Anime(
        id = 1,
        title = "Test Anime",
        description = "Description",
        url = "test-anime",
        poster = "https://example.com/poster.jpg",
        rating = AnimeRating(8.5, 100),
        status = AnimeStatus.ONGOING
    )
    var genresToReturn: List<AnimeGenreFilter> = emptyList()
    var searchFlowToReturn: Flow<PagingData<Anime>> = emptyFlow()

    var shouldThrowError: Boolean = false
    var errorMessage: String = "Repository error"

    var lastRequestedAnimeId: Int? = null
    var lastNeedVideos: Boolean? = null
    var lastSearchQuery: String? = null
    var lastSearchFilter: AnimeFilterState? = null

    override suspend fun getFeed(): Feed {
        if (shouldThrowError) throw IllegalStateException(errorMessage)
        return feedToReturn
    }

    override suspend fun getAnimeDetails(id: Int, needVideos: Boolean): Anime {
        if (shouldThrowError) throw IllegalStateException(errorMessage)
        lastRequestedAnimeId = id
        lastNeedVideos = needVideos
        return animeDetailsToReturn
    }

    override fun searchAnime(query: String, filter: AnimeFilterState): Flow<PagingData<Anime>> {
        lastSearchQuery = query
        lastSearchFilter = filter
        return searchFlowToReturn
    }

    override suspend fun getAnimeGenres(): List<AnimeGenreFilter> {
        if (shouldThrowError) throw IllegalStateException(errorMessage)
        return genresToReturn
    }
}
