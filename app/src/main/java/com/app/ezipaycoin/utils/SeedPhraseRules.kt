package com.app.ezipaycoin.utils

import wallet.core.jni.Mnemonic

/** Input rules for a recovery phrase typed or pasted by the user. */
object SeedPhraseRules {

    /** BIP-39 phrase lengths. */
    val VALID_WORD_COUNTS = setOf(12, 15, 18, 21, 24)

    sealed class Result {
        data class Ok(val normalized: String) : Result()
        data object Empty : Result()
        data object BadLength : Result()
        data object Invalid : Result()
    }

    /** Lower-cases and collapses any run of whitespace (spaces, newlines, pasted tabs) to one space. */
    fun normalize(raw: String): String =
        raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ") { it.lowercase() }

    /** [isValidMnemonic] is the wallet-core check (word list and checksum); tests pass a fake. */
    fun check(raw: String, isValidMnemonic: (String) -> Boolean = { Mnemonic.isValid(it) }): Result {
        val phrase = normalize(raw)
        if (phrase.isEmpty()) return Result.Empty
        if (phrase.split(" ").size !in VALID_WORD_COUNTS) return Result.BadLength
        return if (isValidMnemonic(phrase)) Result.Ok(phrase) else Result.Invalid
    }

    fun message(result: Result): String = when (result) {
        is Result.Ok -> ""
        Result.Empty -> "Please Enter seed phrases"
        Result.BadLength -> "A seed phrase has 12, 15, 18, 21 or 24 words"
        Result.Invalid -> "Invalid seed phrase"
    }
}
