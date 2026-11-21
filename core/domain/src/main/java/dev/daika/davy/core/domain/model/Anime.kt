package dev.daika.davy.core.domain.model

data class Anime(
    val id: Int,
    val title: String,
    val description: String,
    val url: String,
    val poster: String,
    val status: AnimeStatus,

    val otherTitles: List<String> = emptyList(),
    val rating: AnimeRating = AnimeRating(0.0, 0),
    val genres: List<String> = emptyList(),

    val translations: List<AnimeTranslation> = emptyList(),

    val viewingOrder: List<Anime> = emptyList()
) {
    fun hasPlayableEpisodes(): Boolean {
        return translations.any { it.availablePlayers.isNotEmpty() }
    }

    fun isSingleEpisodeTitle(): Boolean {
        val allPlayers = translations.flatMap { it.availablePlayers }
        return allPlayers.isNotEmpty() && allPlayers.all { it.episodes.size <= 1 } && status == AnimeStatus.RELEASED
    }
}
