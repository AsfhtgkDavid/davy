package dev.daika.davy.core.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeVideoDtoTest {

    @Test
    fun `empty list returns empty entity list`() {
        val emptyVideos = emptyList<AnimeVideoDto>()
        assertTrue(emptyVideos.toEntity().isEmpty())
    }

    @Test
    fun `toEntity groups by dubbing and player and sorts players alphabetically`() {
        val videos = listOf(
            AnimeVideoDto(
                id = 1,
                iframeUrl = "//player/1",
                data = PlayerDataDto(player = "ZPlayer", dubbing = "Dubber A"),
                number = "1"
            ),
            AnimeVideoDto(
                id = 2,
                iframeUrl = "//player/2",
                data = PlayerDataDto(player = "APlayer", dubbing = "Dubber A"),
                number = "1"
            ),
            AnimeVideoDto(
                id = 3,
                iframeUrl = "//player/3",
                data = PlayerDataDto(player = "APlayer", dubbing = "Dubber A"),
                number = "2"
            ),
            AnimeVideoDto(
                id = 4,
                iframeUrl = "//player/4",
                data = PlayerDataDto(player = "Kodik", dubbing = "Dubber B"),
                number = "1"
            )
        )

        val translations = videos.toEntity()

        assertEquals(2, translations.size)

        // First translation: "Dubber A"
        val dubberA = translations.first { it.title == "Dubber A" }
        assertEquals(2, dubberA.availablePlayers.size)
        // Players should be sorted: "APlayer" before "ZPlayer"
        assertEquals("APlayer", dubberA.availablePlayers[0].player)
        assertEquals("ZPlayer", dubberA.availablePlayers[1].player)

        // Episodes under "APlayer"
        val aPlayerEpisodes = dubberA.availablePlayers[0].episodes
        assertEquals(2, aPlayerEpisodes.size)
        assertEquals(2, aPlayerEpisodes[0].videoId)
        assertEquals("1", aPlayerEpisodes[0].title)
        assertEquals("//player/2", aPlayerEpisodes[0].iframeUrl)
        assertEquals(3, aPlayerEpisodes[1].videoId)
        assertEquals("2", aPlayerEpisodes[1].title)
        assertEquals("//player/3", aPlayerEpisodes[1].iframeUrl)

        // Second translation: "Dubber B"
        val dubberB = translations.first { it.title == "Dubber B" }
        assertEquals(1, dubberB.availablePlayers.size)
        assertEquals("Kodik", dubberB.availablePlayers[0].player)
        assertEquals(1, dubberB.availablePlayers[0].episodes.size)
    }
}
