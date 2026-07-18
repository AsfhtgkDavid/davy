package dev.daika.davy.domain.usecase

import dev.daika.davy.data.repository.YummyRepository
import javax.inject.Inject

class YummyGetFeedUseCase @Inject constructor(private val repository: YummyRepository) {
    suspend operator fun invoke() = repository.getFeed()
}