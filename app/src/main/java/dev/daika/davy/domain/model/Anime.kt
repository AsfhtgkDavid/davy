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