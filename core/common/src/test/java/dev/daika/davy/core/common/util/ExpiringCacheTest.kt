package dev.daika.davy.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExpiringCacheTest {

    @Test
    fun `get on empty cache returns null`() {
        var currentTime = 1000L
        val cache = ExpiringCache<String, String>(maxSize = 3, ttlMillis = 5000L, clock = { currentTime })

        assertNull(cache.get("nonexistent"))
    }

    @Test
    fun `put and get returns value before ttl expires`() {
        var currentTime = 1000L
        val cache = ExpiringCache<String, String>(maxSize = 3, ttlMillis = 5000L, clock = { currentTime })

        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))

        currentTime += 4999L
        assertEquals("value1", cache.get("key1"))
    }

    @Test
    fun `get returns null after ttl expires and removes item`() {
        var currentTime = 1000L
        val cache = ExpiringCache<String, String>(maxSize = 3, ttlMillis = 5000L, clock = { currentTime })

        cache.put("key1", "value1")
        currentTime += 5001L

        assertNull(cache.get("key1"))

        // Reset clock back to verify it was indeed removed
        currentTime = 1000L
        assertNull(cache.get("key1"))
    }

    @Test
    fun `evicts eldest entry when maxSize is exceeded`() {
        var currentTime = 1000L
        val cache = ExpiringCache<String, String>(maxSize = 2, ttlMillis = 10000L, clock = { currentTime })

        cache.put("k1", "v1")
        cache.put("k2", "v2")
        cache.put("k3", "v3")

        // k1 should be evicted because it was the eldest
        assertNull(cache.get("k1"))
        assertEquals("v2", cache.get("k2"))
        assertEquals("v3", cache.get("k3"))
    }

    @Test
    fun `lru access order protects recently accessed items from eviction`() {
        var currentTime = 1000L
        val cache = ExpiringCache<String, String>(maxSize = 2, ttlMillis = 10000L, clock = { currentTime })

        cache.put("k1", "v1")
        cache.put("k2", "v2")

        // Access k1 so k2 becomes the eldest
        assertEquals("v1", cache.get("k1"))

        // Insert k3 -> k2 should be evicted
        cache.put("k3", "v3")

        assertEquals("v1", cache.get("k1"))
        assertNull(cache.get("k2"))
        assertEquals("v3", cache.get("k3"))
    }

    @Test
    fun `overwriting key updates value and timestamp`() {
        var currentTime = 1000L
        val cache = ExpiringCache<String, String>(maxSize = 2, ttlMillis = 5000L, clock = { currentTime })

        cache.put("k1", "v1")
        currentTime += 4000L // 5000L now

        // Overwrite
        cache.put("k1", "v2")

        // Move clock past original TTL (total +6000L from start, +2000L from overwrite)
        currentTime += 2000L // 7000L now

        // Still valid because overwritten at 5000L with 5000L TTL
        assertEquals("v2", cache.get("k1"))
    }
}
