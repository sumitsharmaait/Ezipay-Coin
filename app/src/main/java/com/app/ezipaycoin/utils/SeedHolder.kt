package com.app.ezipaycoin.utils

/**
 * Holds the recovery phrase between the "write it down" and "confirm it" screens, so it never travels
 * in navigation arguments (which are copied into saved state). Cleared once the wallet is saved and
 * when the confirm screen is left. Memory only: after process death the user starts the step again.
 */
object SeedHolder {
    @Volatile
    private var words: List<String> = emptyList()

    fun set(phrase: List<String>) {
        words = phrase.toList()
    }

    fun peek(): List<String> = words

    fun clear() {
        words = emptyList()
    }
}
