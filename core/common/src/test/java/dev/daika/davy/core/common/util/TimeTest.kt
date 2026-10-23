package dev.daika.davy.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeTest {

    @Test
    fun `formatTime returns 00 00 for negative input`() {
        assertEquals("00:00", formatTime(-1L))
        assertEquals("00:00", formatTime(-1000L))
    }

    @Test
    fun `formatTime returns 00 00 for zero input`() {
        assertEquals("00:00", formatTime(0L))
    }

    @Test
    fun `formatTime formats seconds without hours`() {
        assertEquals("00:05", formatTime(5_000L))
        assertEquals("00:59", formatTime(59_000L))
    }

    @Test
    fun `formatTime formats minutes and seconds without hours`() {
        assertEquals("01:00", formatTime(60_000L))
        assertEquals("01:05", formatTime(65_000L))
        assertEquals("12:34", formatTime((12 * 60 + 34) * 1000L))
        assertEquals("59:59", formatTime((59 * 60 + 59) * 1000L))
    }

    @Test
    fun `formatTime formats hours minutes and seconds`() {
        assertEquals("01:00:00", formatTime(3_600_000L))
        assertEquals("01:02:03", formatTime((3600 + 2 * 60 + 3) * 1000L))
        assertEquals("10:30:15", formatTime((10 * 3600 + 30 * 60 + 15) * 1000L))
    }
}
