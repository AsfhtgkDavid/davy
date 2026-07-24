package dev.daika.davy.domain.entity

enum class AnimeSeason(
    val title: String
) {
    WINTER("Winter"),
    SPRING("Spring"),
    SUMMER("Summer"),
    AUTUMN("Autumn");

    companion object {
        fun fromSeasonNumber(season: Int): AnimeSeason {
            return when (season) {
                1 -> WINTER
                2 -> SPRING
                3 -> SUMMER
                4 -> AUTUMN
                else -> throw IllegalArgumentException("Invalid season number: $season")
            }
        }
    }
}