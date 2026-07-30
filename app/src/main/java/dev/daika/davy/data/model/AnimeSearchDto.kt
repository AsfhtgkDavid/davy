package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeRating

@kotlinx.serialization.Serializable
data class AnimeSearchDto(
    val anime_id: Int,
    val anime_url: String,
    val title: String,
    val description: String?,
    val poster: AnimePostersDto?,
    val rating: AnimeRatingDto?,
    val year: Int?
) {
    fun toEntity(yearFilter: Int? = null): Anime? {
        if (yearFilter != null && year != yearFilter) {
            return null
        }

        return Anime(
            id = anime_id,
            title = title,
            description = description.orEmpty(),
            url = anime_url,
            poster = poster?.medium?.let {
                if (it.startsWith("//")) "https:$it" else it
            }.orEmpty(),
            rating = rating?.toEntity() ?: AnimeRating(0.0, 0)
        )
    }
}