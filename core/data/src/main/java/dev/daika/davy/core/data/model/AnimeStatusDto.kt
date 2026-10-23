package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.AnimeStatus
import kotlinx.serialization.Serializable

@Serializable
data class AnimeStatusDto(
    private val value: Int
) {
    fun toEntity() = AnimeStatus.map[value] ?: AnimeStatus.RELEASED
}
