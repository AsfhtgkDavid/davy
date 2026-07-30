package dev.daika.davy.data.repository

import android.util.LruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.daika.davy.data.api.YummyApi
import dev.daika.davy.data.model.toEntity
import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.Feed
import kotlinx.coroutines.flow.Flow
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
                val videos = yummyApi.getAnimeVideos(id)
                val updatedAnime = cachedAnime.copy(
                    translations = videos.toEntity()
                )
                cache.put(id, updatedAnime)
                updatedAnime
            }
        } else {
            val animeDetails = yummyApi.getAnimeDetails(id, needVideos).toEntity()
            cache.put(id, animeDetails)
            animeDetails
        }
    }

    fun getSearchPagingFlow(query: String): Flow<PagingData<Anime>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { YummySearchPagingSource(this, query) }
        ).flow
    }

    suspend fun searchAnime(query: String): List<Anime> {
        if (query.isBlank()) return emptyList()

        return yummyApi.searchAnime(query.trim())
            .mapNotNull { dto ->
                dto.toEntity()?.also { anime ->
                    cache.put(anime.id, anime)
                }
            }
    }
}