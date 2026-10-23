package dev.daika.davy.core.data.api

import dev.daika.davy.core.data.model.AnimeGenreDto
import dev.daika.davy.core.data.model.AnimeGenreFilterDto
import dev.daika.davy.core.data.model.AnimeGenresResponseDto
import dev.daika.davy.core.data.model.AnimePostersDto
import dev.daika.davy.core.data.model.AnimeRatingDto
import dev.daika.davy.core.data.model.AnimeStatusDto
import dev.daika.davy.core.data.model.AnimeVideoDto
import dev.daika.davy.core.data.model.DetailAnimeDto
import dev.daika.davy.core.data.model.SearchAnimeDto
import dev.daika.davy.core.data.model.TopCarousel
import dev.daika.davy.core.data.model.YummyFeed

class FakeYummyApi : YummyApi {

    val defaultPosters = AnimePostersDto("s", "m", "b", "full.jpg", "h", "mega")
    val defaultRating = AnimeRatingDto(8.0, 100)

    var feedToReturn: YummyFeed = YummyFeed(
        topCarousel = TopCarousel(season = 1, year = 2026, items = emptyList())
    )

    var animeDetailsToReturn: DetailAnimeDto = DetailAnimeDto(
        id = 1,
        title = "Fake Anime",
        description = "Description",
        url = "fake-anime",
        poster = defaultPosters,
        rating = defaultRating,
        genres = listOf(AnimeGenreDto(1, "Action")),
        status = AnimeStatusDto(0)
    )

    var videosToReturn: List<AnimeVideoDto> = emptyList()
    var searchResultsToReturn: List<SearchAnimeDto> = emptyList()
    var genresResponseToReturn: AnimeGenresResponseDto = AnimeGenresResponseDto(
        genres = listOf(
            AnimeGenreFilterDto(
                title = "Action",
                href = "action",
                value = 1,
                moreTitles = emptyList(),
                groupId = 1
            )
        ),
        groups = emptyList()
    )

    var shouldThrowError: Boolean = false
    var errorToThrow: Exception = RuntimeException("Network error")

    var lastSearchQuery: String? = null
    var lastSearchOffset: Int? = null
    var lastSearchLimit: Int? = null
    var getAnimeDetailsCallCount = 0
    var getAnimeVideosCallCount = 0

    override suspend fun getFeed(): YummyFeed {
        if (shouldThrowError) throw errorToThrow
        return feedToReturn
    }

    override suspend fun getAnimeDetails(id: Int, needVideos: Boolean): DetailAnimeDto {
        if (shouldThrowError) throw errorToThrow
        getAnimeDetailsCallCount++
        return animeDetailsToReturn.copy(id = id)
    }

    override suspend fun getAnimeVideos(id: Int): List<AnimeVideoDto> {
        if (shouldThrowError) throw errorToThrow
        getAnimeVideosCallCount++
        return videosToReturn
    }

    override suspend fun searchAnime(
        query: String,
        selectedGenres: Set<String>,
        excludedGenres: Set<String>,
        selectedTypes: Set<String>,
        selectedStatuses: Set<String>,
        yearTo: Int?,
        yearFrom: Int?,
        selectedSort: String?,
        offset: Int,
        limit: Int
    ): List<SearchAnimeDto> {
        if (shouldThrowError) throw errorToThrow
        lastSearchQuery = query
        lastSearchOffset = offset
        lastSearchLimit = limit
        return searchResultsToReturn
    }

    override suspend fun getAnimeGenres(): AnimeGenresResponseDto {
        if (shouldThrowError) throw errorToThrow
        return genresResponseToReturn
    }
}
