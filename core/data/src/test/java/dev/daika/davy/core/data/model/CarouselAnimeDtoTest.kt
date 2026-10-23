package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.AnimeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CarouselAnimeDtoTest {

    private val samplePosters = AnimePostersDto(
        small = "s.jpg",
        medium = "m.jpg",
        big = "b.jpg",
        fullsize = "full.jpg",
        huge = "h.jpg",
        mega = "mega.jpg"
    )

    @Test
    fun `CarouselAnimeDto toEntity correctly maps fields and sets ONGOING status`() {
        val dto = CarouselAnimeDto(
            id = 5,
            title = "Jujutsu Kaisen",
            description = "Sorcerers fighting curses",
            url = "jjk",
            poster = samplePosters,
            rating = AnimeRatingDto(average = 8.8, counters = 2500)
        )

        val entity = dto.toEntity()

        assertEquals(5, entity.id)
        assertEquals("Jujutsu Kaisen", entity.title)
        assertEquals("Sorcerers fighting curses", entity.description)
        assertEquals("jjk", entity.url)
        assertEquals("full.jpg", entity.poster)
        assertEquals(8.8, entity.rating.average, 0.001)
        assertEquals(2500, entity.rating.counters)
        assertEquals(AnimeStatus.ONGOING, entity.status)
    }

    @Test
    fun `YummyFeed toEntity maps season year and items`() {
        val carouselDto = CarouselAnimeDto(
            id = 1,
            title = "Anime 1",
            description = "Desc",
            url = "a1",
            poster = samplePosters,
            rating = AnimeRatingDto(average = 7.0, counters = 50)
        )
        val feed = YummyFeed(
            topCarousel = TopCarousel(
                season = 2,
                year = 2026,
                items = listOf(carouselDto)
            )
        )

        val entity = feed.toEntity()

        assertEquals(2, entity.season)
        assertEquals(2026, entity.year)
        assertEquals(1, entity.items.size)
        assertEquals("Anime 1", entity.items[0].title)
    }
}
