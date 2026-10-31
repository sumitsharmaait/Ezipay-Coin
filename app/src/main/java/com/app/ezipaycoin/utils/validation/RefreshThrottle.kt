package com.app.ezipaycoin.utils.validation

/** Allows an action at most once per [minIntervalMs]. The clock is injectable for tests. */
class RefreshThrottle(
    private val minIntervalMs: Long,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private var last: Long? = null

    /** True (and starts a new window) when enough time has passed, or [force] is set. */
    @Synchronized
    fun tryAcquire(force: Boolean = false): Boolean {
        val now = clock()
        val prev = last
        if (force || prev == null || now - prev >= minIntervalMs) {
            last = now
            return true
        }
        return false
    }
}
