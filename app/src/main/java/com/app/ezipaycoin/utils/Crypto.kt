package com.app.ezipaycoin.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Keystore-backed encryption of the wallet blob. The original CBC key ("secret") is kept so existing
 * data stays readable; new writes use a separate GCM key and the format is described in [EnvelopeCipher].
 */
object Crypto {

    private const val LEGACY_ALIAS = "secret"
    private const val GCM_ALIAS = "secret_gcm_v2"

    private val keyStore = KeyStore
        .getInstance("AndroidKeyStore")
        .apply {
            load(null)
        }

    private val envelope = EnvelopeCipher(
        legacyKey = { keyFor(LEGACY_ALIAS, KeyProperties.BLOCK_MODE_CBC, KeyProperties.ENCRYPTION_PADDING_PKCS7) },
        gcmKey = { keyFor(GCM_ALIAS, KeyProperties.BLOCK_MODE_GCM, KeyProperties.ENCRYPTION_PADDING_NONE) },
        cbcTransformation = "AES/CBC/PKCS7Padding"
    )

    private fun keyFor(alias: String, blockMode: String, padding: String): SecretKey {
        val existing = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existing?.secretKey ?: KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(blockMode)
                        .setEncryptionPaddings(padding)
                        .setRandomizedEncryptionRequired(true)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }
            .generateKey()
    }

    @Synchronized
    fun encrypt(bytes: ByteArray): ByteArray = envelope.encrypt(bytes)

    @Synchronized
    fun decrypt(bytes: ByteArray): ByteArray = envelope.decrypt(bytes)
}
