package dev.daika.davy.core.domain.repository

import androidx.paging.PagingData
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.model.AnimeGenreFilter
import dev.daika.davy.core.domain.model.Feed
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    suspend fun getFeed(): Feed
    suspend fun getAnimeDetails(id: Int, needVideos: Boolean): Anime
    fun searchAnime(query: String, filter: AnimeFilterState): Flow<PagingData<Anime>>
    suspend fun getAnimeGenres(): List<AnimeGenreFilter>
}
