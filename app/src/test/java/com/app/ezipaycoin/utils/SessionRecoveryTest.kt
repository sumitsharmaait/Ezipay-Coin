package com.app.ezipaycoin.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRecoveryTest {

    private var token: String? = "old"
    private var relogins = 0
    private var expired = 0

    private fun recovery(relogin: suspend () -> String?) = SessionRecovery(
        currentToken = { token },
        setToken = { token = it },
        relogin = { relogins++; relogin() },
        onExpired = { expired++ }
    )

    @Test
    fun rejectedSessionSignsInAgainAndRetries() = runTest {
        val r = recovery { "new" }.recover("old")
        assertEquals(SessionRecovery.Outcome.Retry("new"), r)
        assertEquals("new", token)
        assertEquals(0, expired)
    }

    @Test
    fun failedSignInSendsTheAppToUnlockAndKeepsGoingNowhere() = runTest {
        val r = recovery { null }.recover("old")
        assertEquals(SessionRecovery.Outcome.GiveUp, r)
        assertEquals(1, expired)
        assertEquals("old", token)
    }

    @Test
    fun exceptionDuringSignInIsAGiveUp() = runTest {
        val r = recovery { throw java.io.IOException("offline") }.recover("old")
        assertEquals(SessionRecovery.Outcome.GiveUp, r)
        assertEquals(1, expired)
    }

    @Test
    fun concurrentRejectionsShareOneSignIn() = runTest {
        val rec = recovery { "new" }
        val all = (1..5).map { async { rec.recover("old") } }.awaitAll()
        assertEquals(1, relogins)
        assertTrue(all.all { it == SessionRecovery.Outcome.Retry("new") })
    }

    @Test
    fun aTokenRefreshedMeanwhileIsReusedWithoutAnotherSignIn() = runTest {
        token = "fresh"
        val r = recovery { "never" }.recover("old")
        assertEquals(SessionRecovery.Outcome.Retry("fresh"), r)
        assertEquals(0, relogins)
    }

    @Test
    fun onlyAUN001BodyOn401TriggersRecoveryAndOnlyOnce() {
        val ok = SessionRejection::shouldRecover
        assertTrue(ok(401, false, "/api/v1/Dashboard") { "UN001" })
        assertFalse(ok(401, true, "/api/v1/Dashboard") { "UN001" })        // already retried once
        assertFalse(ok(401, false, "/api/v1/Auth/login") { "UN001" })      // never recurse into sign-in
        assertFalse(ok(401, false, "/api/v1/Dashboard") { "Unauthorized" })
        assertFalse(ok(403, false, "/api/v1/Dashboard") { "UN001" })
        assertFalse(ok(500, false, "/api/v1/Dashboard") { "UN001" })
    }

    @Test
    fun expiredSignalFiresOnceUntilANewTokenIsSet() = runTest {
        var count = 0
        val job = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            SessionManager.sessionExpired.collect { count++ }
        }
        SessionManager.token = "x"
        SessionManager.notifySessionExpired()
        SessionManager.notifySessionExpired()
        assertEquals(1, count)
        SessionManager.token = "y"
        SessionManager.notifySessionExpired()
        assertEquals(2, count)
        job.cancel()
        SessionManager.token = ""
    }
}
