package dev.daika.davy.data.repository

import android.util.LruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dev.daika.davy.data.api.YummyApi
import dev.daika.davy.data.model.toEntity
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.Feed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YummyRepository @Inject constructor(
    private val yummyApi: YummyApi
) {
    private val cache = LruCache<Int, Anime>(30)

    suspend fun getFeed(): Feed {
        val feed = yummyApi.getFeed().toEntity()
        return feed

    }

    suspend fun getAnimeDetails(id: Int, needVideos: Boolean): Anime {
        val cachedAnime = cache.get(id)
        return if (cachedAnime != null) {
            cachedAnime
        } else {
            val animeDetails = yummyApi.getAnimeDetails(id, needVideos).toEntity()
            cache.put(id, animeDetails)
            animeDetails
        }
    }

    fun searchAnime(query: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { YummySearchPagingSource(yummyApi, query) }
    ).flow
}