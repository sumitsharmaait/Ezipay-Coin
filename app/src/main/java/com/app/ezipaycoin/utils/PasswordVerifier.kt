package com.app.ezipaycoin.utils

import com.app.ezipaycoin.data.remote.dto.UserPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2-HMAC-SHA256 verifier for the app password. Only this record is stored; the password
 * itself is never written. Encoded as `v1:pbkdf2-sha256:<iterations>:<salt b64>:<hash b64>`, so the
 * iteration count can be raised later without invalidating existing records.
 */
object PasswordVerifier {

    const val DEFAULT_ITERATIONS = 310_000
    const val MIN_ITERATIONS = 210_000
    private const val SALT_BYTES = 16
    private const val HASH_BITS = 256
    private const val PREFIX = "v1:pbkdf2-sha256:"
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    private val random = SecureRandom()

    fun create(password: String, iterations: Int = DEFAULT_ITERATIONS, salt: ByteArray = newSalt()): String {
        require(iterations > 0) { "iterations must be positive" }
        val hash = derive(password, salt, iterations)
        val b64 = Base64.getEncoder()
        return "$PREFIX$iterations:${b64.encodeToString(salt)}:${b64.encodeToString(hash)}"
    }

    /** False for a malformed record, a wrong password or an empty password. Never throws. */
    fun verify(encoded: String?, password: String): Boolean {
        if (encoded == null || password.isEmpty() || !encoded.startsWith(PREFIX)) return false
        return try {
            val parts = encoded.removePrefix(PREFIX).split(":")
            if (parts.size != 3) return false
            val iterations = parts[0].toInt()
            if (iterations <= 0) return false
            val dec = Base64.getDecoder()
            val salt = dec.decode(parts[1])
            val expected = dec.decode(parts[2])
            if (salt.isEmpty() || expected.isEmpty()) return false
            MessageDigest.isEqual(expected, derive(password, salt, iterations))
        } catch (e: Exception) {
            false
        }
    }

    private fun newSalt(): ByteArray = ByteArray(SALT_BYTES).also { random.nextBytes(it) }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

/** Password checks against the stored record, covering both the verifier and a not yet migrated plaintext. */
object PasswordCredentials {

    fun hasPassword(prefs: UserPreferences): Boolean =
        !prefs.passwordVerifier.isNullOrEmpty() || !prefs.password.isNullOrEmpty()

    /** The verifier wins whenever it exists; the legacy plaintext is only used before migration. */
    fun matches(prefs: UserPreferences, entered: String): Boolean {
        if (!prefs.passwordVerifier.isNullOrEmpty()) return PasswordVerifier.verify(prefs.passwordVerifier, entered)
        val legacy = prefs.password
        if (legacy.isNullOrEmpty() || entered.isEmpty()) return false
        return MessageDigest.isEqual(legacy.toByteArray(Charsets.UTF_8), entered.toByteArray(Charsets.UTF_8))
    }

    /** Stores a new password as a verifier and drops any legacy plaintext in the same write. */
    fun withPassword(prefs: UserPreferences, password: String): UserPreferences =
        prefs.copy(passwordVerifier = PasswordVerifier.create(password), password = null)
}
