package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.Feed
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
