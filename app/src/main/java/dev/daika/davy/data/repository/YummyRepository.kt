package dev.daika.davy.data.repository

import android.util.LruCache
import dev.daika.davy.data.api.YummyApi
import dev.daika.davy.domain.model.Anime
import dev.daika.davy.domain.model.YummyFeed
import javax.inject.Inject

class YummyRepository @Inject constructor(
    private val yummyApi: YummyApi
) {
    private val cache = LruCache<Int, Anime>(30)

    suspend fun getFeed(): YummyFeed {
        val feed = yummyApi.getFeed()
        feed.topCarousel.items.forEach { anime ->
            cache.put(anime.id, anime)
        }
        return feed

    }

    suspend fun getAnimeDetails(id: Int): Anime {
        val cachedAnime = cache.get(id)
        return if (cachedAnime != null) {
            cachedAnime
        } else {
            val animeDetails = yummyApi.getAnimeDetails(id)
            cache.put(id, animeDetails)
            animeDetails
        }
    }
}