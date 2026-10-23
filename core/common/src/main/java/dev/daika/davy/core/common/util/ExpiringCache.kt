package dev.daika.davy.core.common.util

import android.os.SystemClock

class ExpiringCache<K, V>(
    maxSize: Int,
    private val ttlMillis: Long,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private val map = object : LinkedHashMap<K, Entry<V>>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<K?, Entry<V>?>?): Boolean {
            return size > maxSize
        }
    }

    data class Entry<V>(val value: V, val timestamp: Long)

    fun get(key: K): V? {
        synchronized(this) {
            val v = map.get(key) ?: return null
            if (clock() - v.timestamp > ttlMillis) {
                map.remove(key)
                return null
            }
            return v.value
        }
    }

    fun put(key: K, value: V) =
        synchronized(this) {
            map.put(key, Entry(value, clock()))
        }
}
