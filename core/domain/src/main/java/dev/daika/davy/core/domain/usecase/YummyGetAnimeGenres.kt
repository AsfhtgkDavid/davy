package dev.daika.davy.core.domain.usecase

import dev.daika.davy.core.domain.repository.AnimeRepository
import javax.inject.Inject

class YummyGetAnimeGenres @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke() = repository.getAnimeGenres()
}
