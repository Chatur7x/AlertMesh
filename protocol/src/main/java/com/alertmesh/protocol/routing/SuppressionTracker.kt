package com.alertmesh.protocol.routing

import java.util.concurrent.ConcurrentHashMap

class SuppressionTracker {
    private val tracker = ConcurrentHashMap<String, MutableSet<String>>()
    private val timestamps = ConcurrentHashMap<String, Long>()

    fun recordRebroadcast(msgId: String, peerId: String) {
        tracker.computeIfAbsent(msgId) { ConcurrentHashMap.newKeySet() }.add(peerId)
        timestamps[msgId] = System.currentTimeMillis()
    }

    fun shouldSuppress(msgId: String, threshold: Int = 2): Boolean {
        val peers = tracker[msgId]
        return peers != null && peers.size >= threshold
    }
}
