package dev.daika.davy.core.domain.model

data class FilterOption(
    val id: String,
    val title: String
)

data class AnimeFilterState(
    val selectedGenres: Set<String> = emptySet(),
    val excludedGenres: Set<String> = emptySet(),
    val selectedTypes: Set<String> = emptySet(),
    val selectedStatuses: Set<String> = emptySet(),
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    val selectedSort: String? = null
) {
    fun hasActiveFilters(): Boolean =
        selectedGenres.isNotEmpty() ||
                excludedGenres.isNotEmpty() ||
                selectedTypes.isNotEmpty() ||
                selectedStatuses.isNotEmpty() ||
                yearFrom != null ||
                yearTo != null ||
                (selectedSort != null && selectedSort != "none")
}

val ALL_GENRES = emptyList<FilterOption>()

val ALL_TYPES = listOf(
    FilterOption("tv", "TV Series"),
    FilterOption("movie", "Movie"),
    FilterOption("ova", "OVA"),
    FilterOption("ona", "ONA"),
    FilterOption("special", "Special"),
    FilterOption("shortfilm", "Short Film"),
    FilterOption("shorttv", "Short TV")
)

val ALL_STATUSES = listOf(
    FilterOption("released", "Released"),
    FilterOption("ongoing", "Ongoing"),
    FilterOption("announcement", "Announcement")
)

val ALL_SORT_OPTIONS = listOf(
    FilterOption("none", "Relevance"),
    FilterOption("rating", "Rating"),
    FilterOption("rating_counters", "Rating Count"),
    FilterOption("views", "Views"),
    FilterOption("year", "Year"),
    FilterOption("title", "Title"),
    FilterOption("top", "Top")
)

val ALL_YEARS = listOf(FilterOption("none", "Any")) + (2026 downTo 1980).map {
    FilterOption(it.toString(), it.toString())
}
