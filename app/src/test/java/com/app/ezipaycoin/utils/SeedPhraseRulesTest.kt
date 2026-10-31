package com.app.ezipaycoin.utils

import com.app.ezipaycoin.utils.SeedPhraseRules.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedPhraseRulesTest {

    private fun words(n: Int) = (1..n).joinToString(" ") { "word$it" }
    private val accept: (String) -> Boolean = { true }

    @Test
    fun everyBip39LengthIsAccepted() {
        for (n in listOf(12, 15, 18, 21, 24)) {
            assertEquals("length $n", Result.Ok(words(n)), SeedPhraseRules.check(words(n), accept))
        }
    }

    @Test
    fun otherLengthsAreRejected() {
        for (n in listOf(1, 11, 13, 14, 16, 23, 25)) {
            assertEquals("length $n", Result.BadLength, SeedPhraseRules.check(words(n), accept))
        }
    }

    @Test
    fun emptyAndBlankInput() {
        assertEquals(Result.Empty, SeedPhraseRules.check("", accept))
        assertEquals(Result.Empty, SeedPhraseRules.check("  \n\t ", accept))
    }

    @Test
    fun checksumFailureFromWalletCoreIsInvalid() {
        assertEquals(Result.Invalid, SeedPhraseRules.check(words(24)) { false })
    }

    @Test
    fun pastedWhitespaceAndCaseAreNormalised() {
        val raw = "  Word1  word2\nWORD3\tword4 word5 word6 word7 word8 word9 word10 word11 word12  \n"
        val r = SeedPhraseRules.check(raw, accept) as Result.Ok
        assertEquals("word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 word11 word12", r.normalized)
    }

    @Test
    fun validatorSeesTheNormalisedPhrase() {
        var seen = ""
        SeedPhraseRules.check("  A  b c d e f g h i j k L ") { seen = it; true }
        assertEquals("a b c d e f g h i j k l", seen)
    }

    @Test
    fun messagesAreSpecificPerCase() {
        assertEquals("", SeedPhraseRules.message(Result.Ok("x")))
        assertTrue(SeedPhraseRules.message(Result.BadLength).contains("24"))
        assertEquals("Invalid seed phrase", SeedPhraseRules.message(Result.Invalid))
    }
}
