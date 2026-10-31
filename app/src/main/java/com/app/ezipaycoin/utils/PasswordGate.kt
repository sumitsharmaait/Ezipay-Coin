package com.app.ezipaycoin.utils

import java.security.MessageDigest

/**
 * Checks a re-entered app password with a constant-time compare and a short lockout after
 * repeated failures. Used before the seed phrase / private key is shown.
 */
class PasswordGate(
    private val clock: () -> Long = System::currentTimeMillis,
    private val maxAttempts: Int = 5,
    private val lockoutMillis: Long = 30_000L
) {
    sealed class Result {
        data object Ok : Result()
        data class Wrong(val attemptsLeft: Int) : Result()
        data class LockedOut(val retryAfterMillis: Long) : Result()
    }

    private var failures = 0
    private var lockedUntil = 0L

    fun check(expected: String?, entered: String): Result {
        val now = clock()
        if (now < lockedUntil) return Result.LockedOut(lockedUntil - now)
        if (now >= lockedUntil && lockedUntil != 0L) {
            failures = 0
            lockedUntil = 0L
        }
        val ok = !expected.isNullOrEmpty() && MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            entered.toByteArray(Charsets.UTF_8)
        )
        if (ok) {
            failures = 0
            return Result.Ok
        }
        failures++
        if (failures >= maxAttempts) {
            lockedUntil = now + lockoutMillis
            return Result.LockedOut(lockoutMillis)
        }
        return Result.Wrong(maxAttempts - failures)
    }
}
