package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.AnimeRating
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