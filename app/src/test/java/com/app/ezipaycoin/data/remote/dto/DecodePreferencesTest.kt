package com.app.ezipaycoin.data.remote.dto

import androidx.datastore.core.CorruptionException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Base64

class DecodePreferencesTest {

    private val identity: (ByteArray) -> ByteArray = { it }

    private fun encode(p: UserPreferences): ByteArray =
        Base64.getEncoder().encode(Json.encodeToString(p).toByteArray())

    @Test
    fun emptyFileIsAFreshInstall() {
        assertNull(decodePreferences(ByteArray(0), identity))
    }

    @Test
    fun validRecordRoundTrips() {
        val prefs = UserPreferences(isWalletCreated = true, walletAddress = "0xabc", seedPhrase = "a b c")
        assertEquals(prefs, decodePreferences(encode(prefs), identity))
    }

    @Test
    fun decryptionFailureIsCorruptionNotAnEmptyRecord() {
        val failing: (ByteArray) -> ByteArray = { throw java.security.GeneralSecurityException("key gone") }
        try {
            decodePreferences(encode(UserPreferences(isWalletCreated = true)), failing)
            fail("expected CorruptionException")
        } catch (e: CorruptionException) {
            assertTrue(e.cause is java.security.GeneralSecurityException)
        }
    }

    @Test
    fun garbageBytesAreCorruption() {
        try {
            decodePreferences("not base64 !!".toByteArray(), identity)
            fail("expected CorruptionException")
        } catch (e: CorruptionException) {
            // expected
        }
    }

    @Test
    fun invalidJsonIsCorruption() {
        try {
            decodePreferences(Base64.getEncoder().encode("{broken".toByteArray()), identity)
            fail("expected CorruptionException")
        } catch (e: CorruptionException) {
            // expected
        }
    }
}
