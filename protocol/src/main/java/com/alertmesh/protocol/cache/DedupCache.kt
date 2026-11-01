package com.alertmesh.protocol.cache

class DedupCache(capacity: Int = 5000, private val ttlMs: Long = 300_000) { // 5 min TTL
    private val entries = object : java.util.LinkedHashMap<String, Long>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return size > capacity
        }
    }

    @Synchronized
    fun isDuplicate(msgId: String): Boolean {
        evict()
        return if (entries.containsKey(msgId)) {
            true
        } else {
            entries[msgId] = System.currentTimeMillis()
            false
        }
    }

    @Synchronized
    fun add(msgId: String) {
        evict()
        entries[msgId] = System.currentTimeMillis()
    }

    private fun evict() {
        val cutoff = System.currentTimeMillis() - ttlMs
        entries.entries.removeIf { it.value < cutoff }
    }
}
