package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.AnimeRating
import kotlinx.serialization.Serializable

@Serializable
data class AnimeRatingDto(
    val average: Double,
    val counters: Int
) {
    fun toEntity() = AnimeRating(
        average = average,
        counters = counters
    )
}
