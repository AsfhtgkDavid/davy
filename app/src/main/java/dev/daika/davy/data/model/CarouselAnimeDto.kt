package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.Anime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CarouselAnimeDto(
    @SerialName("anime_id")
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("anime_url")
    val url: String,
    val poster: AnimePostersDto,
    val rating: AnimeRatingDto,
) {
    fun toEntity() = Anime(
        id = id,
        title = title,
        description = description,
        url = url,
        poster = poster.fullsize,
        rating = rating.toEntity()
    )
}


