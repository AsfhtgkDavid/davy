package dev.daika.davy.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YummyFeed(
    @SerialName("top_carousel")
    val topCarousel: TopCarousel,
)

@Serializable
data class TopCarousel(
    val season: Int,
    val year: Int,
    val items: List<Anime>
)
