package dev.daika.davy.data.repository

import android.util.LruCache
import dev.daika.davy.data.api.YummyApi
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.Feed
import javax.inject.Inject

class YummyRepository @Inject constructor(
    private val yummyApi: YummyApi
) {
    private val cache = LruCache<Int, Anime>(30)

    suspend fun getFeed(): Feed {
        val feed = yummyApi.getFeed().toEntity()
        feed.items.forEach { anime ->
            cache.put(anime.id, anime)
        }
        return feed

    }

    suspend fun getAnimeDetails(id: Int, needVideos: Boolean): Anime {
        val cachedAnime = cache.get(id)
        return if (cachedAnime != null) {
            if (!needVideos || cachedAnime.translations.isNotEmpty()) {
                cachedAnime
            } else {
                // TODO: There is /videos endpoint need to use it instead of fetching all details again
                val animeDetails = yummyApi.getAnimeDetails(id, true).toEntity()
                cache.put(id, animeDetails)
                animeDetails
            }
        } else {
            val animeDetails = yummyApi.getAnimeDetails(id, needVideos).toEntity()
            cache.put(id, animeDetails)
            animeDetails
        }
    }
}