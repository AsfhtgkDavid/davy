package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeRating
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetailAnimeDto(
    @SerialName("anime_id")
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("anime_url")
    val url: String,
    val poster: AnimePostersDto,
    val rating: AnimeRatingDto,
    val genres: List<AnimeGenreDto>,
    @SerialName("other_titles")
    val otherTitles: List<String> = emptyList(),
    val videos: List<AnimeVideoDto> = emptyList(),
    @SerialName("viewing_order")
    val viewingOrder: List<ViewingOrderAnimeDto> = emptyList()
) {
    fun toEntity() = Anime(
        id = id,
        title = title,
        description = description,
        url = url,
        poster = poster.fullsize,
        rating = rating.toEntity(),
        genres = genres.map { it.toEntity() },
        otherTitles = otherTitles,
        translations = videos.toEntity(),
        viewingOrder = viewingOrder.map { it.toEntity() }
    )
}

@Serializable
data class ViewingOrderAnimeDto(
    @SerialName("anime_id")
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("anime_url")
    val url: String,
    val poster: AnimePostersDto,
    val rating: Double,
) {
    fun toEntity() = Anime(
        id = id,
        title = title,
        description = description,
        url = url,
        poster = poster.fullsize,
        rating = AnimeRating(rating, 0),
    )
}

@Serializable
data class AnimeGenreDto(
    val id: Int,
    val title: String,
) {
    fun toEntity() = title
}