package com.alertmesh.protocol

import com.alertmesh.protocol.cache.ReplayGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayGuardTest {

    @Test
    fun testFirstMessageForOriginAccepted() {
        val guard = ReplayGuard()
        assertFalse("first message should not be a replay", guard.isReplay("alice", 1))
    }

    @Test
    fun testInOrderDeliveryAccepted() {
        val guard = ReplayGuard()
        assertFalse(guard.isReplay("alice", 1))
        guard.record("alice", 1)
        assertFalse(guard.isReplay("alice", 2))
        guard.record("alice", 2)
    }

    @Test
    fun testOutOfOrderWithinWindowAccepted() {
        val guard = ReplayGuard()
        guard.record("alice", 5)
        // Counter 3 arrives after 5 (out of order) but is within the 100 window -> accept
        assertFalse("out-of-order counter within window should be accepted",
            guard.isReplay("alice", 3))
        guard.record("alice", 3)
    }

    @Test
    fun testDuplicateRejected() {
        val guard = ReplayGuard()
        guard.record("alice", 5)
        assertTrue("exact duplicate should be rejected", guard.isReplay("alice", 5))
    }

    @Test
    fun testOutsideWindowRejected() {
        val guard = ReplayGuard()
        guard.record("alice", 100)
        // Counter 0 is exactly one window below the high water mark -> reject
        assertTrue("counter outside window should be rejected", guard.isReplay("alice", 0))
        // Counter 1 is 99 below -> within window -> accept
        assertFalse("counter inside window should be accepted", guard.isReplay("alice", 1))
    }

    @Test
    fun testOriginsTrackedIndependently() {
        val guard = ReplayGuard()
        guard.record("alice", 50)
        // Bob's counter 40 is unrelated to Alice's high water mark -> accept
        assertFalse("different origins are independent", guard.isReplay("bob", 40))
    }
}
