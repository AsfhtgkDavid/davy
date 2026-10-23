package dev.daika.davy.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnimePostersDto(
    val small: String,
    val medium: String,
    val big: String,
    val fullsize: String,
    val huge: String,
    val mega: String,
)
