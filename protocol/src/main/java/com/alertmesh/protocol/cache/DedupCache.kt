package com.alertmesh.protocol.cache

class DedupCache(capacity: Int = 5000) {
    private val map = object : java.util.LinkedHashMap<String, Boolean>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > capacity
        }
    }

    @Synchronized
    fun isDuplicate(msgId: String): Boolean {
        return if (map.containsKey(msgId)) {
            true
        } else {
            map[msgId] = true
            false
        }
    }

    @Synchronized
    fun add(msgId: String) {
        map[msgId] = true
    }
}
