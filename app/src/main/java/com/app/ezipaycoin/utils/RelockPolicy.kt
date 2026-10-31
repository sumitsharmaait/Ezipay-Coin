package com.app.ezipaycoin.utils

/**
 * Decides when an unlocked app must ask for the password again: after it has been in the
 * background for at least [timeoutMillis]. Quick app switches and rotations do not relock.
 */
class RelockPolicy(
    private val clock: () -> Long = System::currentTimeMillis,
    val timeoutMillis: Long = DEFAULT_TIMEOUT_MS
) {
    private var backgroundedAt: Long? = null

    fun onBackground() {
        if (backgroundedAt == null) backgroundedAt = clock()
    }

    /** True when the time away reached the timeout. Resets the timer either way. */
    fun onForeground(): Boolean {
        val since = backgroundedAt ?: return false
        backgroundedAt = null
        return clock() - since >= timeoutMillis
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 2 * 60 * 1000L
    }
}
