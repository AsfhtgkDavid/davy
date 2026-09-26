package dev.daika.davy.domain.entity

enum class AnimeStatus(private val id: Int) {
    RELEASED(0),
    ONGOING(1),
    ANNOUNCE(2);

    companion object {
        val map = values().associateBy(AnimeStatus::id)
    }
}