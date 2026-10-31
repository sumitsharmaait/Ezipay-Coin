package com.app.ezipaycoin.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RelockPolicyTest {

    private var now = 10_000L
    private val policy = RelockPolicy(clock = { now }, timeoutMillis = 120_000L)

    @Test
    fun defaultTimeoutIsTwoMinutes() {
        assertEquals(120_000L, RelockPolicy.DEFAULT_TIMEOUT_MS)
    }

    @Test
    fun staysUnlockedOnQuickReturn() {
        policy.onBackground()
        now += 119_999
        assertFalse(policy.onForeground())
    }

    @Test
    fun locksAtTheTimeout() {
        policy.onBackground()
        now += 120_000
        assertTrue(policy.onForeground())
    }

    @Test
    fun foregroundWithoutBackgroundNeverLocks() {
        assertFalse(policy.onForeground())
    }

    @Test
    fun timerResetsAfterReturn() {
        policy.onBackground()
        now += 200_000
        assertTrue(policy.onForeground())
        assertFalse(policy.onForeground())
        policy.onBackground()
        now += 1_000
        assertFalse(policy.onForeground())
    }

    @Test
    fun repeatedStopsDoNotRestartTheClock() {
        policy.onBackground()
        now += 100_000
        policy.onBackground()
        now += 25_000
        assertTrue(policy.onForeground())
    }
}
