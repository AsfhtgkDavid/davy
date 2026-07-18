package dev.daika.davy.data.repository

import dev.daika.davy.data.api.YummyApi
import javax.inject.Inject

class YummyRepository @Inject constructor(
    private val yummyApi: YummyApi
) {
    suspend fun getFeed() = yummyApi.getFeed()
    suspend fun getAnimeDetails(id: String) = yummyApi.getAnimeDetails(id)
}