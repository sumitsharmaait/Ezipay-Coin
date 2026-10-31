package com.app.ezipaycoin.utils

import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

/**
 * Versioned storage format for the wallet blob.
 *  - legacy: IV(16) + AES/CBC/PKCS7 ciphertext, key [legacyKey]
 *  - v2:     MAGIC(4) + IV(12) + AES/GCM ciphertext+tag, key [gcmKey]
 * Writes use v2 and are read back before they are returned; if that check fails the legacy format is
 * written instead, so a write never produces data this class cannot read. Reads try v2 when the magic
 * is present and fall back to legacy, since a legacy IV can start with the same four bytes.
 * Keys are supplied lazily so tests can use plain AES keys instead of the Android keystore.
 */
class EnvelopeCipher(
    private val legacyKey: () -> SecretKey,
    private val gcmKey: () -> SecretKey,
    private val cbcTransformation: String = "AES/CBC/PKCS5Padding"
) {
    fun encrypt(plain: ByteArray): ByteArray {
        val v2 = try {
            encryptGcm(plain).takeIf { decryptGcm(it).contentEquals(plain) }
        } catch (e: GeneralSecurityException) {
            null
        }
        return v2 ?: encryptLegacy(plain)
    }

    fun decrypt(stored: ByteArray): ByteArray {
        if (hasMagic(stored)) {
            try {
                return decryptGcm(stored)
            } catch (e: GeneralSecurityException) {
                // Not a v2 record after all (or damaged): the legacy path decides.
            }
        }
        return decryptLegacy(stored)
    }

    fun isLegacy(stored: ByteArray): Boolean = !hasMagic(stored)

    private fun hasMagic(b: ByteArray) =
        b.size > MAGIC.size + GCM_IV_BYTES && MAGIC.indices.all { b[it] == MAGIC[it] }

    private fun encryptGcm(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance(GCM)
        c.init(Cipher.ENCRYPT_MODE, gcmKey())
        return MAGIC + c.iv + c.doFinal(plain)
    }

    private fun decryptGcm(stored: ByteArray): ByteArray {
        val iv = stored.copyOfRange(MAGIC.size, MAGIC.size + GCM_IV_BYTES)
        val data = stored.copyOfRange(MAGIC.size + GCM_IV_BYTES, stored.size)
        val c = Cipher.getInstance(GCM)
        c.init(Cipher.DECRYPT_MODE, gcmKey(), GCMParameterSpec(128, iv))
        return c.doFinal(data)
    }

    private fun encryptLegacy(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance(cbcTransformation)
        c.init(Cipher.ENCRYPT_MODE, legacyKey())
        return c.iv + c.doFinal(plain)
    }

    private fun decryptLegacy(stored: ByteArray): ByteArray {
        val c = Cipher.getInstance(cbcTransformation)
        val iv = stored.copyOfRange(0, c.blockSize)
        val data = stored.copyOfRange(c.blockSize, stored.size)
        c.init(Cipher.DECRYPT_MODE, legacyKey(), IvParameterSpec(iv))
        return c.doFinal(data)
    }

    companion object {
        private val MAGIC = byteArrayOf(0x45, 0x5A, 0x47, 0x32) // "EZG2"
        private const val GCM_IV_BYTES = 12
        private const val GCM = "AES/GCM/NoPadding"
    }
}
