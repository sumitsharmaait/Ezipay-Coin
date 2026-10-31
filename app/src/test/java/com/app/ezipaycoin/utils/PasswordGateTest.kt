package com.app.ezipaycoin.utils

import com.app.ezipaycoin.utils.PasswordGate.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGateTest {

    private var now = 1_000L
    private fun gate() = PasswordGate(clock = { now }, maxAttempts = 3, lockoutMillis = 30_000L)

    @Test
    fun correctPasswordIsAccepted() {
        assertEquals(Result.Ok, gate().check("secret123", "secret123"))
    }

    @Test
    fun wrongPasswordCountsDown() {
        val g = gate()
        assertEquals(Result.Wrong(2), g.check("secret123", "nope"))
        assertEquals(Result.Wrong(1), g.check("secret123", "nope"))
    }

    @Test
    fun missingStoredPasswordNeverMatches() {
        assertTrue(gate().check(null, "") is Result.Wrong)
        assertTrue(gate().check("", "") is Result.Wrong)
    }

    @Test
    fun lockoutAfterMaxAttemptsEvenForCorrectPassword() {
        val g = gate()
        g.check("p", "x"); g.check("p", "x")
        assertTrue(g.check("p", "x") is Result.LockedOut)
        now += 10_000
        val locked = g.check("p", "p")
        assertTrue(locked is Result.LockedOut)
        assertEquals(20_000L, (locked as Result.LockedOut).retryAfterMillis)
    }

    @Test
    fun lockoutEndsAndCountersReset() {
        val g = gate()
        repeat(3) { g.check("p", "x") }
        now += 30_001
        assertEquals(Result.Ok, g.check("p", "p"))
        assertEquals(Result.Wrong(2), g.check("p", "x"))
    }

    @Test
    fun successResetsFailureCount() {
        val g = gate()
        g.check("p", "x")
        g.check("p", "p")
        assertEquals(Result.Wrong(2), g.check("p", "x"))
    }

    @Test
    fun matcherOverloadUsesTheCallerDecision() {
        val g = gate()
        assertEquals(Result.Ok, g.check("x") { true })
        assertEquals(Result.Wrong(2), g.check("x") { false })
    }
}
