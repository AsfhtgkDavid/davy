package dev.daika.davy.core.data.api

import dev.daika.davy.core.data.model.AnimeGenresResponseDto
import dev.daika.davy.core.data.model.AnimeVideoDto
import dev.daika.davy.core.data.model.DetailAnimeDto
import dev.daika.davy.core.data.model.SearchAnimeDto
import dev.daika.davy.core.data.model.YummyFeed
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YummyApi {
    @GET("/feed")
    suspend fun getFeed(): YummyFeed

    @GET("/anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") id: Int,
        @Query("need_videos") needVideos: Boolean = false
    ): DetailAnimeDto

    @GET("/anime/{id}/videos")
    suspend fun getAnimeVideos(
        @Path("id") id: Int
    ): List<AnimeVideoDto>

    @GET("/anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("genres") selectedGenres: Set<String> = emptySet(),
        @Query("exclude_genres") excludedGenres: Set<String> = emptySet(),
        @Query("types") selectedTypes: Set<String> = emptySet(),
        @Query("status") selectedStatuses: Set<String> = emptySet(),
        @Query("to_year") yearTo: Int? = null,
        @Query("from_year") yearFrom: Int? = null,
        @Query("sort") selectedSort: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<SearchAnimeDto>

    @GET("/anime/genres")
    suspend fun getAnimeGenres(): AnimeGenresResponseDto
}
