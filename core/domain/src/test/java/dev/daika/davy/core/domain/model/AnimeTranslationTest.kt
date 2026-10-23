package dev.daika.davy.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AnimeTranslationTest {

    @Test
    fun `getPlayerByEpisodeId returns player when episode exists`() {
        val ep1 = AnimeEpisode(videoId = 101, title = "Episode 1", iframeUrl = "//player.com/1")
        val ep2 = AnimeEpisode(videoId = 102, title = "Episode 2", iframeUrl = "//player.com/2")
        val ep3 = AnimeEpisode(videoId = 201, title = "Episode 1", iframeUrl = "//player2.com/1")

        val player1 = AnimePlayer(player = "Kodik", episodes = listOf(ep1, ep2))
        val player2 = AnimePlayer(player = "Alloha", episodes = listOf(ep3))

        val translation1 = AnimeTranslation(
            title = "Studio Band",
            availablePlayers = listOf(player1)
        )
        val translation2 = AnimeTranslation(
            title = "AniLibria",
            availablePlayers = listOf(player2)
        )

        val translations = listOf(translation1, translation2)

        val foundPlayer = translations.getPlayerByEpisodeId(102)
        assertNotNull(foundPlayer)
        assertEquals("Kodik", foundPlayer?.player)

        val foundPlayer2 = translations.getPlayerByEpisodeId(201)
        assertNotNull(foundPlayer2)
        assertEquals("Alloha", foundPlayer2?.player)
    }

    @Test
    fun `getPlayerByEpisodeId returns null when episode does not exist`() {
        val player = AnimePlayer(
            player = "Kodik",
            episodes = listOf(AnimeEpisode(videoId = 1, title = "1", iframeUrl = "//url/1"))
        )
        val translation = AnimeTranslation(title = "Dub", availablePlayers = listOf(player))

        assertNull(listOf(translation).getPlayerByEpisodeId(999))
        assertNull(emptyList<AnimeTranslation>().getPlayerByEpisodeId(1))
    }
}
