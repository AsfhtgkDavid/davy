package dev.daika.davy.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnimeStatusTest {

    @Test
    fun `enum ids match expected constants`() {
        assertEquals(0, AnimeStatus.RELEASED.id)
        assertEquals(1, AnimeStatus.ONGOING.id)
        assertEquals(2, AnimeStatus.ANNOUNCE.id)
    }

    @Test
    fun `companion map correctly resolves statuses by id`() {
        assertEquals(AnimeStatus.RELEASED, AnimeStatus.map[0])
        assertEquals(AnimeStatus.ONGOING, AnimeStatus.map[1])
        assertEquals(AnimeStatus.ANNOUNCE, AnimeStatus.map[2])
    }

    @Test
    fun `companion map returns null for unknown id`() {
        assertNull(AnimeStatus.map[-1])
        assertNull(AnimeStatus.map[999])
    }
}
