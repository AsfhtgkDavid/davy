package dev.daika.davy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class YummyFeed(
    val announcements: List<Anime>,
)

