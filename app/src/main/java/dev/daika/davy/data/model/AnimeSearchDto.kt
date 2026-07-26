package dev.daika.davy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnimeSearchDto(
    val anime_id: Int,
    val anime_url: String,
    val title: String,
    val description: String?,
    val poster: AnimePostersDto?,
    val rating: AnimeRatingDto?,
    val year: Int?
)