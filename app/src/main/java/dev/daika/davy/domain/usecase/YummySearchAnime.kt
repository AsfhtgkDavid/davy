package dev.daika.davy.domain.usecase

import dev.daika.davy.data.repository.YummyRepository
import javax.inject.Inject

class YummySearchAnime @Inject constructor(private val repository: YummyRepository) {
    suspend operator fun invoke(query: String) =
        repository.searchAnime(query)
}