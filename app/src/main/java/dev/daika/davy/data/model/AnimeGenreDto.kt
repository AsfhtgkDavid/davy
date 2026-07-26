package dev.daika.davy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnimeGenreDto(
    val id: Int,
    val title: String,
) {
    fun toEntity() = title
}