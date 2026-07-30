package com.alertmesh.protocol.cache

import java.util.concurrent.ConcurrentHashMap

class ReplayGuard {
    private val highWaterMarks = ConcurrentHashMap<String, Long>()

    fun isReplay(origin: String, counter: Long): Boolean {
        val current = highWaterMarks[origin] ?: -1L
        return counter <= current
    }

    fun record(origin: String, counter: Long) {
        highWaterMarks.compute(origin) { _, current ->
            if (current == null || counter > current) counter else current
        }
    }
}
