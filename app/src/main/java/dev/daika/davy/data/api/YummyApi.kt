package dev.daika.davy.data.api

import dev.daika.davy.domain.model.Anime
import dev.daika.davy.domain.model.YummyFeed
import retrofit2.http.GET
import retrofit2.http.Path

interface YummyApi {
    @GET("/feed")
    suspend fun getFeed(): YummyFeed

    @GET("/anime/{id}")
    suspend fun getAnimeDetails(@Path("id") id: Int): Anime
}