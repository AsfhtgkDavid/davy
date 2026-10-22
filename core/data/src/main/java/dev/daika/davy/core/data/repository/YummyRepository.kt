package dev.daika.davy.core.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.daika.davy.core.common.util.ExpiringCache
import dev.daika.davy.core.data.api.YummyApi
import dev.daika.davy.core.data.model.AnimeGenreFilterDto
import dev.daika.davy.core.data.model.toEntity
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.AnimeGenreFilter
import dev.daika.davy.core.domain.model.Feed
import dev.daika.davy.core.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "YummyRepository"
private const val GENRES_CACHE_TTL_MILLIS = 7L * 24 * 60 * 60 * 1000

@Singleton
class YummyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val yummyApi: YummyApi
) : AnimeRepository {
    private val animeCache = ExpiringCache<Int, Anime>(5, 3_600_000)
    private val cache by lazy {
        context.cacheDir
    }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getFeed(): Feed {
        return yummyApi.getFeed().toEntity()
    }

    override suspend fun getAnimeDetails(id: Int, needVideos: Boolean): Anime {
        val cachedAnime = animeCache.get(id)
        return if (cachedAnime != null) {
            if (!needVideos || cachedAnime.translations.isNotEmpty()) {
                cachedAnime
            } else {
                val translations = yummyApi.getAnimeVideos(id).toEntity()
                val animeDetails = cachedAnime.copy(translations = translations)
                animeCache.put(id, animeDetails)
                animeDetails
            }
        } else {
            val animeDetails = yummyApi.getAnimeDetails(id, needVideos).toEntity()
            animeCache.put(id, animeDetails)
            animeDetails
        }
    }

    override fun searchAnime(query: String, filter: AnimeFilterState): Flow<PagingData<Anime>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { YummySearchPagingSource(yummyApi, query, filter) }
    ).flow

    override suspend fun getAnimeGenres(): List<AnimeGenreFilter> {
        readGenresFromDisk()?.let { return it }

        val genres = yummyApi.getAnimeGenres().genres.map { it.toEntity() }
        writeGenresToDisk(genres)
        return genres
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readGenresFromDisk(): List<AnimeGenreFilter>? {
        val cacheFile = File(cache, "genres.json")
        if (!cacheFile.exists()) return null
        val raw = cacheFile.inputStream()
        return try {
            val cached = raw.use { json.decodeFromStream<GenresDiskCache>(it) }
            if (System.currentTimeMillis() - cached.savedAtMillis > GENRES_CACHE_TTL_MILLIS) {
                cacheFile.delete()
                null
            } else {
                cached.genres.map { it.toEntity() }
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Failed to decode genres cache", e)
            cacheFile.delete()
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeGenresToDisk(genres: List<AnimeGenreFilter>) {
        val payload = GenresDiskCache(
            savedAtMillis = System.currentTimeMillis(),
            genres = genres.map {
                AnimeGenreFilterDto(
                    title = it.title,
                    href = it.id,
                    value = 0,
                    moreTitles = emptyList(),
                    groupId = 0
                )
            }
        )
        File(cache, "genres.json").outputStream().use { json.encodeToStream(payload, it) }
    }

    @Serializable
    private data class GenresDiskCache(
        val savedAtMillis: Long,
        val genres: List<AnimeGenreFilterDto>
    )
}
