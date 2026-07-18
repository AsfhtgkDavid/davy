package dev.daika.davy.data.api

import dev.daika.davy.domain.model.YummyFeed
import retrofit2.http.GET

interface YummyApi {
    @GET("/feed")
    suspend fun getFeed(): YummyFeed

    @GET("/anime/{id}")
    suspend fun getAnimeDetails(id: String): String
}