package dev.daika.davy.core.ui.component

import dev.daika.davy.core.domain.model.FilterOption

fun formatSelectionHint(
    selectedIds: Set<String>,
    options: List<FilterOption>,
    defaultHint: String
): String {
    if (selectedIds.isEmpty()) return defaultHint
    val selectedTitles = options.filter { it.id in selectedIds }.map { it.title }
    return when {
        selectedTitles.isEmpty() -> defaultHint
        selectedTitles.size == 1 -> selectedTitles.first()
        selectedTitles.size == 2 -> selectedTitles.joinToString(", ")
        else -> "${selectedTitles.take(2).joinToString(", ")} (+${selectedTitles.size - 2})"
    }
}
