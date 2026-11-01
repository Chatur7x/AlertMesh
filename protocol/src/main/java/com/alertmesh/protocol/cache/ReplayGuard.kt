package com.alertmesh.protocol.cache

import java.util.concurrent.ConcurrentHashMap

class ReplayGuard(private val windowSize: Int = 100) {
    private val highWaterMarks = ConcurrentHashMap<String, Long>()
    private val seenCounters = ConcurrentHashMap<String, MutableSet<Long>>()

    fun isReplay(origin: String, counter: Long): Boolean {
        val highWater = highWaterMarks[origin] ?: -1L

        val seen = seenCounters[origin] ?: mutableSetOf()

        // Already seen this exact counter = replay
        if (counter in seen) return true

        // Counter is at or below high water and outside the reordering window = replay.
        // Counters within the window below the high water are allowed (out-of-order delivery).
        if (counter <= highWater && highWater - counter >= windowSize) return true

        return false
    }

    fun record(origin: String, counter: Long) {
        highWaterMarks.compute(origin) { _, current ->
            if (current == null || counter > current) counter else current
        }

        // Track seen counters within the window so duplicates are still rejected.
        val seen = seenCounters.computeIfAbsent(origin) { ConcurrentHashMap.newKeySet() }
        seen.add(counter)

        // Prune old counters: remove those far below the high water mark.
        val highWater = highWaterMarks[origin] ?: counter
        seen.removeIf { highWater - it >= windowSize }
    }
}
