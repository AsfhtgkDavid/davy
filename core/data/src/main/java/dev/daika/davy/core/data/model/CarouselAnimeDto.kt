package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeStatus
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
        rating = rating.toEntity(),
        status = AnimeStatus.ONGOING,
    )
}
