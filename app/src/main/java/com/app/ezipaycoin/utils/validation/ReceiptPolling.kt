package com.app.ezipaycoin.utils.validation

/** Backoff schedule for waiting on a transaction receipt. */
object ReceiptPolling {
    /** Delays (ms) that grow by half each step up to [maxStepMs] and add up to about [totalMs]. */
    fun schedule(totalMs: Long = 60_000, firstMs: Long = 2_000, maxStepMs: Long = 8_000): List<Long> {
        val delays = mutableListOf<Long>()
        var step = firstMs
        var sum = 0L
        while (sum < totalMs) {
            val d = minOf(step, totalMs - sum)
            delays += d
            sum += d
            step = minOf(step * 3 / 2, maxStepMs)
        }
        return delays
    }
}
