package dev.daika.davy.domain.entity

data class Feed(
    val season: Int,
    val year: Int,
    val items: List<Anime>
)