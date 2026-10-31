package com.app.ezipaycoin.utils.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshThrottleTest {
    @Test
    fun allowsFirstThenBlocksWithinWindow() {
        var now = 1_000L
        val t = RefreshThrottle(10_000) { now }
        assertTrue(t.tryAcquire())
        now += 9_999
        assertFalse(t.tryAcquire())
        now += 1
        assertTrue(t.tryAcquire())
    }

    @Test
    fun forceBypassesAndRestartsWindow() {
        var now = 0L
        val t = RefreshThrottle(10_000) { now }
        assertTrue(t.tryAcquire())
        now += 100
        assertTrue(t.tryAcquire(force = true))
        now += 5_000
        assertFalse(t.tryAcquire())
    }
}
