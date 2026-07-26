package dev.daika.davy.domain.usecase

import dev.daika.davy.data.repository.YummyRepository
import dev.daika.davy.domain.entity.Anime
import javax.inject.Inject

class YummySearchAnimeUseCase @Inject constructor(
    private val repository: YummyRepository
) {

    suspend operator fun invoke(query: String): List<Anime> {
        return repository.searchAnime(query)
    }
}