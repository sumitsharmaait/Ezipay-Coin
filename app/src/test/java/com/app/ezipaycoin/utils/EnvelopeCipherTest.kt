package com.app.ezipaycoin.utils

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EnvelopeCipherTest {

    private fun key(): SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val cbc = key()
    private val gcm = key()
    private val cipher = EnvelopeCipher({ cbc }, { gcm })
    private val data = """{"password":null,"seedPhrase":"a b c"}""".toByteArray()

    private fun legacyBlob(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, cbc)
        return c.iv + c.doFinal(plain)
    }

    @Test
    fun newWritesAreGcmAndRoundTrip() {
        val enc = cipher.encrypt(data)
        assertFalse(cipher.isLegacy(enc))
        assertArrayEquals(data, cipher.decrypt(enc))
    }

    @Test
    fun existingCbcBlobsStillDecrypt() {
        val old = legacyBlob(data)
        assertTrue(cipher.isLegacy(old))
        assertArrayEquals(data, cipher.decrypt(old))
    }

    @Test
    fun legacyBlobWhoseIvStartsWithTheMagicStillDecrypts() {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = byteArrayOf(0x45, 0x5A, 0x47, 0x32, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        c.init(Cipher.ENCRYPT_MODE, cbc, javax.crypto.spec.IvParameterSpec(iv))
        val blob = iv + c.doFinal(data)
        assertArrayEquals(data, cipher.decrypt(blob))
    }

    @Test
    fun tamperedGcmRecordIsRejected() {
        val enc = cipher.encrypt(data)
        enc[enc.size - 1] = (enc[enc.size - 1].toInt() xor 1).toByte()
        try {
            cipher.decrypt(enc)
            throw AssertionError("expected failure")
        } catch (e: java.security.GeneralSecurityException) {
            // integrity check works
        }
    }

    @Test
    fun fallsBackToLegacyWhenGcmKeyIsUnusable() {
        val broken = EnvelopeCipher({ cbc }, { throw java.security.InvalidKeyException("no gcm key") })
        val enc = broken.encrypt(data)
        assertTrue(broken.isLegacy(enc))
        assertArrayEquals(data, cipher.decrypt(enc))
    }

    @Test
    fun eachWriteUsesAFreshIv() {
        assertFalse(cipher.encrypt(data).contentEquals(cipher.encrypt(data)))
        assertEquals(cipher.encrypt(data).size, cipher.encrypt(data).size)
    }
}
