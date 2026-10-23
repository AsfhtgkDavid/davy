package dev.daika.davy.core.domain.usecase

import dev.daika.davy.core.domain.model.AnimeFilterState
import dev.daika.davy.core.domain.repository.AnimeRepository
import javax.inject.Inject

class YummySearchAnime @Inject constructor(
    private val repository: AnimeRepository
) {
    operator fun invoke(query: String, filter: AnimeFilterState) =
        repository.searchAnime(query, filter)
}
