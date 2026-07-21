package dev.daika.davy.domain.usecase

import dev.daika.davy.data.repository.YummyRepository
import javax.inject.Inject

class YummyGetAnimeUseCase @Inject constructor(private val repository: YummyRepository) {
    suspend operator fun invoke(id: Int, needVideos: Boolean) =
        repository.getAnimeDetails(id, needVideos)
}