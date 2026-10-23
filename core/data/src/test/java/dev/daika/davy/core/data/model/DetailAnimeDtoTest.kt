package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.AnimeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DetailAnimeDtoTest {

    private val samplePosters = AnimePostersDto(
        small = "small.jpg",
        medium = "medium.jpg",
        big = "big.jpg",
        fullsize = "fullsize.jpg",
        huge = "huge.jpg",
        mega = "mega.jpg"
    )

    @Test
    fun `DetailAnimeDto toEntity correctly maps all fields and viewingOrder`() {
        val dto = DetailAnimeDto(
            id = 10,
            title = "Test Anime",
            description = "Test Description",
            url = "test-anime",
            poster = samplePosters,
            rating = AnimeRatingDto(average = 8.5, counters = 120),
            genres = listOf(AnimeGenreDto(id = 1, title = "Action")),
            otherTitles = listOf("Alias 1", "Alias 2"),
            videos = emptyList(),
            viewingOrder = listOf(
                ViewingOrderAnimeDto(
                    id = 10, // Same ID as parent
                    title = "Test Anime Season 1",
                    description = "Season 1",
                    url = "test-anime-s1",
                    poster = samplePosters,
                    rating = 0.0, // Should be replaced with parent rating (8.5)
                    status = AnimeStatusDto(0)
                ),
                ViewingOrderAnimeDto(
                    id = 11, // Different ID
                    title = "Test Anime Season 2",
                    description = "Season 2",
                    url = "test-anime-s2",
                    poster = samplePosters,
                    rating = 9.0, // Should remain 9.0
                    status = AnimeStatusDto(1)
                )
            ),
            status = AnimeStatusDto(1)
        )

        val entity = dto.toEntity()

        assertEquals(10, entity.id)
        assertEquals("Test Anime", entity.title)
        assertEquals("Test Description", entity.description)
        assertEquals("test-anime", entity.url)
        assertEquals("fullsize.jpg", entity.poster)
        assertEquals(8.5, entity.rating.average, 0.001)
        assertEquals(120, entity.rating.counters)
        assertEquals(listOf("Action"), entity.genres)
        assertEquals(listOf("Alias 1", "Alias 2"), entity.otherTitles)
        assertEquals(AnimeStatus.ONGOING, entity.status)

        // Verify viewingOrder logic:
        assertEquals(2, entity.viewingOrder.size)
        // Item with same ID (10) uses parent's rating
        assertEquals(8.5, entity.viewingOrder[0].rating.average, 0.001)
        assertEquals(120, entity.viewingOrder[0].rating.counters)
        // Item with different ID (11) retains its own rating with 0 counters
        assertEquals(9.0, entity.viewingOrder[1].rating.average, 0.001)
        assertEquals(0, entity.viewingOrder[1].rating.counters)
    }
}
