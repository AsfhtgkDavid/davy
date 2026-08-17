package dev.daika.davy.data.api

import dev.daika.davy.data.model.AnimeVideoDto
import dev.daika.davy.data.model.DetailAnimeDto
import dev.daika.davy.data.model.SearchAnimeDto
import dev.daika.davy.data.model.YummyFeed
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
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<SearchAnimeDto>
}