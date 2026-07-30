package dev.daika.davy.domain.usecase

import androidx.paging.PagingData
import dev.daika.davy.data.repository.YummyRepository
import dev.daika.davy.domain.entity.Anime
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class YummySearchAnimeUseCase @Inject constructor(
    private val repository: YummyRepository
) {
    fun getPagingFlow(query: String): Flow<PagingData<Anime>> {
        return repository.getSearchPagingFlow(query)
    }

    suspend operator fun invoke(query: String): List<Anime> {
        return repository.searchAnime(query)
    }
}