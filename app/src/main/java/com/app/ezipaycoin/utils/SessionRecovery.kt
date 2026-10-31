package com.app.ezipaycoin.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Handles a rejected session (HTTP 401 UN001). The first caller signs in again with the stored wallet;
 * callers that arrive while that runs, or after it, reuse the new token. If signing in fails, [onExpired]
 * asks the app to show the unlock screen. The wallet is never touched, and each request is retried once
 * by the caller.
 */
class SessionRecovery(
    private val currentToken: () -> String?,
    private val setToken: (String) -> Unit,
    private val relogin: suspend () -> String?,
    private val onExpired: () -> Unit
) {
    sealed class Outcome {
        data class Retry(val token: String) : Outcome()
        data object GiveUp : Outcome()
    }

    private val mutex = Mutex()

    suspend fun recover(rejectedToken: String?): Outcome = mutex.withLock {
        val now = currentToken()
        if (!now.isNullOrEmpty() && now != rejectedToken) return Outcome.Retry(now)
        val fresh = try {
            relogin()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        if (fresh.isNullOrEmpty()) {
            onExpired()
            Outcome.GiveUp
        } else {
            setToken(fresh)
            Outcome.Retry(fresh)
        }
    }
}
