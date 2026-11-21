package dev.daika.davy.core.domain.model

data class Feed(
    val season: Int,
    val year: Int,
    val items: List<Anime>
) {
    fun getSeasonTitle(): String {
        val animeSeason = AnimeSeason.fromSeasonNumber(season)
        return "${animeSeason.title}'s 20${year} Anime"
    }
}
