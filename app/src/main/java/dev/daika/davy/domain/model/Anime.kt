package dev.daika.davy.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    @SerialName("anime_id")
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("anime_url")
    val url: String,
    val poster: AnimePosters,
    val rating: AnimeRating,

    @SerialName("other_titles")
    val otherTitles: List<String>? = null,
    val genres: List<AnimeGenre>? = null,

    val videos: List<AnimeVideoDto>? = null,
)

@Serializable
data class AnimePosters(
    val small: String,
    val medium: String,
    val big: String,
    val fullsize: String,
    val huge: String,
    val mega: String,
)

@Serializable
data class AnimeRating(
    val average: Double,
    val counters: Int
)

@Serializable
data class AnimeGenre(
    val id: Int,
    val title: String,
)

@Serializable
data class AnimeVideoDto(
    @SerialName("video_id")
    val id: Int,
    @SerialName("iframe_url")
    val iframeUrl: String,
    val data: PlayerDataDto,
)

@Serializable
data class PlayerDataDto(
    val player: String,
    val dubbing: String,
)