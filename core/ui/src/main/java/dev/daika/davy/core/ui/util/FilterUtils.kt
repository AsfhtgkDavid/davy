package dev.daika.davy.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.daika.davy.core.domain.model.AnimeSeason
import dev.daika.davy.core.domain.model.Feed
import dev.daika.davy.core.domain.model.FilterOption
import dev.daika.davy.core.ui.R

@Composable
fun FilterOption.getDisplayTitle(): String {
    return when (id) {
        "tv" -> stringResource(R.string.type_tv)
        "movie" -> stringResource(R.string.type_movie)
        "ova" -> stringResource(R.string.type_ova)
        "ona" -> stringResource(R.string.type_ona)
        "special" -> stringResource(R.string.type_special)
        "shortfilm" -> stringResource(R.string.type_shortfilm)
        "shorttv" -> stringResource(R.string.type_shorttv)
        "released" -> stringResource(R.string.status_released)
        "ongoing" -> stringResource(R.string.status_ongoing)
        "announcement" -> stringResource(R.string.status_announcement)
        "none" -> if (title == "Any") stringResource(R.string.filter_year_any) else stringResource(R.string.sort_relevance)
        "rating" -> stringResource(R.string.sort_rating)
        "rating_counters" -> stringResource(R.string.sort_rating_count)
        "views" -> stringResource(R.string.sort_views)
        "year" -> stringResource(R.string.sort_year)
        "title" -> stringResource(R.string.sort_title)
        "top" -> stringResource(R.string.sort_top)
        else -> title
    }
}

@Composable
fun Feed.getDisplaySeasonTitle(): String {
    val seasonName = when (AnimeSeason.fromSeasonNumber(season)) {
        AnimeSeason.WINTER -> stringResource(R.string.season_winter)
        AnimeSeason.SPRING -> stringResource(R.string.season_spring)
        AnimeSeason.SUMMER -> stringResource(R.string.season_summer)
        AnimeSeason.AUTUMN -> stringResource(R.string.season_autumn)
    }
    return stringResource(R.string.feed_season_title, seasonName, year)
}

@Composable
fun List<FilterOption>.formatSelectionHint(
    selectedIds: Set<String>,
    defaultHint: String
): String {
    if (selectedIds.isEmpty()) return defaultHint
    val selectedTitles = filter { it.id in selectedIds }.map { it.getDisplayTitle() }
    return when {
        selectedTitles.isEmpty() -> defaultHint
        selectedTitles.size == 1 -> selectedTitles.first()
        selectedTitles.size == 2 -> selectedTitles.joinToString(", ")
        else -> stringResource(
            R.string.filter_selection_hint_more,
            selectedTitles.take(2).joinToString(", "),
            selectedTitles.size - 2
        )
    }
}

