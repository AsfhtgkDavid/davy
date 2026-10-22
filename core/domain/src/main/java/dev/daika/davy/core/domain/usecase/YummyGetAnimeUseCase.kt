package dev.daika.davy.core.domain.usecase

import dev.daika.davy.core.domain.repository.AnimeRepository
import javax.inject.Inject

class YummyGetAnimeUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(id: Int, needVideos: Boolean = false) =
        repository.getAnimeDetails(id, needVideos)
}
