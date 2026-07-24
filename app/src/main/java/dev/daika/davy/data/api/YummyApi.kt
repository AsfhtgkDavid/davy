package dev.daika.davy.data.api

import dev.daika.davy.data.model.DetailAnimeDto
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
}