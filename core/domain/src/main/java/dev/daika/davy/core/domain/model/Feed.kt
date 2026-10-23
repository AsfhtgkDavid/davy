package dev.daika.davy.core.domain.model

data class Feed(
    val season: Int,
    val year: Int,
    val items: List<Anime>
)
