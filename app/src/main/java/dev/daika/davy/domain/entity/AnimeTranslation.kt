package dev.daika.davy.domain.entity

data class AnimeTranslation(
    val title: String,
    val availablePlayers: List<AnimePlayer>
)

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