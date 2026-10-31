package com.app.ezipaycoin.utils

import com.app.ezipaycoin.data.remote.dto.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordVerifierTest {

    private val fast = 1_000

    @Test
    fun correctPasswordVerifies() {
        val v = PasswordVerifier.create("correct horse", fast)
        assertTrue(PasswordVerifier.verify(v, "correct horse"))
    }

    @Test
    fun wrongOrEmptyPasswordIsRejected() {
        val v = PasswordVerifier.create("correct horse", fast)
        assertFalse(PasswordVerifier.verify(v, "correct hors"))
        assertFalse(PasswordVerifier.verify(v, ""))
        assertFalse(PasswordVerifier.verify(null, "correct horse"))
    }

    @Test
    fun recordDoesNotContainThePassword() {
        val v = PasswordVerifier.create("hunter2hunter2", fast)
        assertFalse(v.contains("hunter2"))
    }

    @Test
    fun saltIsRandomPerRecord() {
        assertNotEquals(PasswordVerifier.create("same-password", fast), PasswordVerifier.create("same-password", fast))
    }

    @Test
    fun defaultsMeetTheMinimumAndRecordTheIterationCount() {
        assertTrue(PasswordVerifier.DEFAULT_ITERATIONS >= 210_000)
        val v = PasswordVerifier.create("password-1")
        assertTrue(v.startsWith("v1:pbkdf2-sha256:${PasswordVerifier.DEFAULT_ITERATIONS}:"))
        val salt = java.util.Base64.getDecoder().decode(v.split(":")[3])
        assertEquals(16, salt.size)
        assertTrue(PasswordVerifier.verify(v, "password-1"))
    }

    @Test
    fun malformedRecordsNeverThrow() {
        for (bad in listOf("", "x", "v1:pbkdf2-sha256:", "v1:pbkdf2-sha256:abc:AA:AA", "v1:pbkdf2-sha256:5:%%%:%%%", "v1:pbkdf2-sha256:0:AA:AA")) {
            assertFalse(PasswordVerifier.verify(bad, "anything"))
        }
    }

    @Test
    fun credentialsPreferVerifierOverLegacyPlaintext() {
        val prefs = UserPreferences(password = "old-plain", passwordVerifier = PasswordVerifier.create("new-pass", fast))
        assertTrue(PasswordCredentials.matches(prefs, "new-pass"))
        assertFalse(PasswordCredentials.matches(prefs, "old-plain"))
    }

    @Test
    fun credentialsAcceptLegacyPlaintextBeforeMigration() {
        val prefs = UserPreferences(password = "old-plain")
        assertTrue(PasswordCredentials.hasPassword(prefs))
        assertTrue(PasswordCredentials.matches(prefs, "old-plain"))
        assertFalse(PasswordCredentials.matches(prefs, "other"))
        assertFalse(PasswordCredentials.matches(UserPreferences(), ""))
        assertFalse(PasswordCredentials.hasPassword(UserPreferences()))
    }

    @Test
    fun withPasswordDropsPlaintext() {
        val out = PasswordCredentials.withPassword(UserPreferences(password = "old-plain"), "brand-new")
        assertNull(out.password)
        assertTrue(PasswordCredentials.matches(out, "brand-new"))
    }
}
