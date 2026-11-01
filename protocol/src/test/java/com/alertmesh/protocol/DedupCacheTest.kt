package com.alertmesh.protocol

import com.alertmesh.protocol.cache.DedupCache
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DedupCacheTest {
    @Test
    fun testDuplicateDetection() {
        val cache = DedupCache()
        assertFalse(cache.isDuplicate("msg-1"))
        assertTrue(cache.isDuplicate("msg-1"))
    }

    @Test
    fun testDifferentMessages() {
        val cache = DedupCache()
        assertFalse(cache.isDuplicate("msg-1"))
        assertFalse(cache.isDuplicate("msg-2"))
    }

    @Test
    fun testEviction() {
        val cache = DedupCache(capacity = 2)
        cache.add("msg-1")
        cache.add("msg-2")
        cache.add("msg-3") // Evicts msg-1
        
        assertFalse("msg-1 should be evicted", cache.isDuplicate("msg-1"))
        assertTrue("msg-3 should still be in cache", cache.isDuplicate("msg-3"))
    }

    @Test
    fun testTimeBasedExpiration() {
        val cache = DedupCache(capacity = 1000, ttlMs = 50)
        assertFalse(cache.isDuplicate("msg-1")) // registers msg-1
        assertTrue(cache.isDuplicate("msg-1")) // immediately after -> duplicate
        Thread.sleep(100) // let TTL elapse
        assertFalse("msg-1 should expire after TTL", cache.isDuplicate("msg-1"))
    }
}
