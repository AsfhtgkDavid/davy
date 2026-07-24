package dev.daika.davy.domain.entity

data class AnimeTranslation(
    val title: String,
    val availablePlayers: List<AnimePlayer>
)

fun List<AnimeTranslation>.getIframeById(episodeId: Int): String? {
    return this.asSequence()
        .flatMap { it.availablePlayers }
        .flatMap { it.episodes }
        .firstOrNull { it.videoId == episodeId }
        ?.iframeUrl
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