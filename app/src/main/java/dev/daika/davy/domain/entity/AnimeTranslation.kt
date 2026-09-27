package dev.daika.davy.domain.entity

import dev.daika.davyparsers.Parser

data class AnimeTranslation(
    val title: String,
    val availablePlayers: List<AnimePlayer>
)

fun AnimePlayer.isSupported(parsers: List<Parser>): Boolean {
    return episodes.any { episode ->
        Parser.getParserForUrl("https:${episode.iframeUrl}", parsers) != null
    }
}

fun AnimeTranslation.withSupportedPlayers(parsers: List<Parser>): AnimeTranslation {
    return copy(
        availablePlayers = availablePlayers.filter { it.isSupported(parsers) }
    )
}

fun List<AnimeTranslation>.filterSupportedPlayers(parsers: List<Parser>): List<AnimeTranslation> {
    return map { it.withSupportedPlayers(parsers) }
        .filter { it.availablePlayers.isNotEmpty() }
}

fun List<AnimeTranslation>.getPlayerByEpisodeId(episodeId: Int): AnimePlayer? {
    return this.firstNotNullOfOrNull { translation ->
        translation.availablePlayers.firstOrNull { player ->
            player.episodes.any { episode -> episode.videoId == episodeId }
        }
    }
}

data class AnimePlayer(
    val player: String,
    val episodes: List<AnimeEpisode>
)

data class AnimeEpisode(
    val videoId: Int,
    val title: String,
    val iframeUrl: String
)