package dev.daika.davy.core.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeFilterStateTest {

    @Test
    fun `default state has no active filters`() {
        val state = AnimeFilterState()
        assertFalse(state.hasActiveFilters())
    }

    @Test
    fun `selectedSort none does not count as active filter`() {
        val state = AnimeFilterState(selectedSort = "none")
        assertFalse(state.hasActiveFilters())
    }

    @Test
    fun `selectedSort with valid sort option counts as active filter`() {
        val state = AnimeFilterState(selectedSort = "rating")
        assertTrue(state.hasActiveFilters())
    }

    @Test
    fun `selectedGenres counts as active filter`() {
        val state = AnimeFilterState(selectedGenres = setOf("action"))
        assertTrue(state.hasActiveFilters())
    }

    @Test
    fun `excludedGenres counts as active filter`() {
        val state = AnimeFilterState(excludedGenres = setOf("horror"))
        assertTrue(state.hasActiveFilters())
    }

    @Test
    fun `selectedTypes counts as active filter`() {
        val state = AnimeFilterState(selectedTypes = setOf("tv"))
        assertTrue(state.hasActiveFilters())
    }

    @Test
    fun `selectedStatuses counts as active filter`() {
        val state = AnimeFilterState(selectedStatuses = setOf("ongoing"))
        assertTrue(state.hasActiveFilters())
    }

    @Test
    fun `yearFrom counts as active filter`() {
        val state = AnimeFilterState(yearFrom = 2020)
        assertTrue(state.hasActiveFilters())
    }

    @Test
    fun `yearTo counts as active filter`() {
        val state = AnimeFilterState(yearTo = 2024)
        assertTrue(state.hasActiveFilters())
    }
}
