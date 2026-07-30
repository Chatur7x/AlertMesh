package com.alertmesh.protocol.routing

import java.util.concurrent.ConcurrentHashMap

class RateLimiter(
    private val maxTokens: Int = 20,
    private val refillRatePerSecond: Double = 5.0
) {
    private class TokenBucket(val maxTokens: Int) {
        var tokens: Double = maxTokens.toDouble()
        var lastRefillTime: Long = System.currentTimeMillis()
    }

    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    fun tryConsume(peerId: String): Boolean {
        val bucket = buckets.computeIfAbsent(peerId) { TokenBucket(maxTokens) }
        synchronized(bucket) {
            val now = System.currentTimeMillis()
            val elapsedMs = now - bucket.lastRefillTime
            val newTokens = elapsedMs * (refillRatePerSecond / 1000.0)
            
            bucket.tokens = Math.min(maxTokens.toDouble(), bucket.tokens + newTokens)
            bucket.lastRefillTime = now

            return if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0
                true
            } else {
                false
            }
        }
    }
}
