package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.AnimeStatus
import kotlinx.serialization.Serializable

@Serializable
data class AnimeStatusDto(
    private val value: Int
) {
    fun toEntity() = AnimeStatus.map[value] ?: AnimeStatus.RELEASED
}