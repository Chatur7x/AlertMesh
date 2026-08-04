package com.alertmesh.protocol.routing

import java.util.concurrent.ConcurrentHashMap

class SuppressionTracker(private val maxAgeMs: Long = 60_000) {
    private val tracker = ConcurrentHashMap<String, MutableSet<String>>()
    private val timestamps = ConcurrentHashMap<String, Long>()

    fun evict() {
        val now = System.currentTimeMillis()
        val toRemove = timestamps.filter { now - it.value > maxAgeMs }.keys
        toRemove.forEach {
            tracker.remove(it)
            timestamps.remove(it)
        }
    }

    fun recordRebroadcast(msgId: String, peerId: String) {
        evict()
        tracker.computeIfAbsent(msgId) { ConcurrentHashMap.newKeySet() }.add(peerId)
        timestamps[msgId] = System.currentTimeMillis()
    }

    fun shouldSuppress(msgId: String, threshold: Int = 2): Boolean {
        val peers = tracker[msgId]
        return peers != null && peers.size >= threshold
    }
}
