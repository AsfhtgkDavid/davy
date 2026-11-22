package dev.daika.davy.core.common.util

class ExpiringCache<K, V>(
    maxSize: Int,
    private val ttlMillis: Long,
    private val clock: () -> Long = { System.nanoTime() / 1_000_000L }
) {
    private val map = object : LinkedHashMap<K, Entry<V>>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<K, Entry<V>>): Boolean {
            return size > maxSize
        }
    }

    private data class Entry<V>(val value: V, val timestamp: Long)

    fun get(key: K): V? = synchronized(this) {
        val v = map[key] ?: return null
        if (clock() - v.timestamp > ttlMillis) {
            map.remove(key)
            return null
        }
        v.value
    }

    fun put(key: K, value: V) {
        synchronized(this) {
            map[key] = Entry(value, clock())
        }
    }
}