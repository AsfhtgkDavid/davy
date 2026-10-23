package dev.daika.davy.core.data.model

import dev.daika.davy.core.domain.model.AnimeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AnimeStatusDtoTest {

    @Test
    fun `toEntity maps known integer values correctly`() {
        assertEquals(AnimeStatus.RELEASED, AnimeStatusDto(0).toEntity())
        assertEquals(AnimeStatus.ONGOING, AnimeStatusDto(1).toEntity())
        assertEquals(AnimeStatus.ANNOUNCE, AnimeStatusDto(2).toEntity())
    }

    @Test
    fun `toEntity defaults to RELEASED for unknown values`() {
        assertEquals(AnimeStatus.RELEASED, AnimeStatusDto(-1).toEntity())
        assertEquals(AnimeStatus.RELEASED, AnimeStatusDto(99).toEntity())
    }
}
