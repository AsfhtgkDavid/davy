package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.Feed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YummyFeed(
    @SerialName("top_carousel")
    val topCarousel: TopCarousel
) {
    fun toEntity() = Feed(
        season = topCarousel.season,
        year = topCarousel.year,
        items = topCarousel.items.map { it.toEntity() }
    )
}

@Serializable
data class TopCarousel(
    val season: Int,
    val year: Int,
    val items: List<CarouselAnimeDto>,
)
