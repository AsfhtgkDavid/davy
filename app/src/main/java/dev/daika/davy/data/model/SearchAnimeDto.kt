package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeRating
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchAnimeDto(
    @SerialName("anime_id")
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("anime_url")
    val url: String,
    val poster: AnimePostersDto,
    val rating: AnimeRatingDto,
    val genres: List<AnimeGenreDto>,
) {
    fun toEntity() = Anime(
        id = id,
        title = title,
        description = description,
        url = url,
        poster = poster.fullsize,
        rating = rating.toEntity(),
        genres = genres.map { it.toEntity() },
    )
}
