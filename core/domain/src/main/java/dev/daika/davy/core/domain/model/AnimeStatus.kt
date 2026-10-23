package dev.daika.davy.core.domain.model

enum class AnimeStatus(val id: Int) {
    RELEASED(0),
    ONGOING(1),
    ANNOUNCE(2);

    companion object {
        val map = values().associateBy(AnimeStatus::id)
    }
}
