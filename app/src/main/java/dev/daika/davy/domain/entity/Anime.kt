package dev.daika.davy.domain.entity

data class Anime(
    val id: Int,
    val title: String,
    val description: String,
    val url: String,
    val poster: String,

    val otherTitles: List<String> = emptyList(),
    val rating: AnimeRating = AnimeRating(0.0, 0),
    val genres: List<String> = emptyList(),

    val translations: List<AnimeTranslation> = emptyList(),

    val viewingOrder: List<Anime> = emptyList()
)
