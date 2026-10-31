package com.app.ezipaycoin.data.remote.dto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.app.ezipaycoin.utils.Crypto
import com.app.ezipaycoin.utils.userPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class UserPreferences(
    val isWalletCreated: Boolean = false,
    val password: String? = null,
    val walletPrivateKey: String? = null,
    val walletAddress: String? = null,
    val nonce: String? = null,
    val token: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userProfile: String? = null,
    val seedPhrase: String? = null,
    val bioMetricAuthEnabled: Boolean = false,
    val isWalletReadyForAuth: Boolean = false,
)


/**
 * Decodes the stored ciphertext. An empty file is a fresh install and gives null. Anything that
 * cannot be decrypted or parsed raises [CorruptionException]; it is never turned into an empty
 * record, because the next write would then replace the only on-device copy of the wallet.
 */
internal fun decodePreferences(bytes: ByteArray, decrypt: (ByteArray) -> ByteArray): UserPreferences? {
    if (bytes.isEmpty()) return null
    return try {
        val decoded = Base64.getDecoder().decode(bytes)
        val json = decrypt(decoded).decodeToString()
        Json.decodeFromString<UserPreferences>(json)
    } catch (e: Exception) {
        throw CorruptionException("Stored wallet data cannot be read", e)
    }
}

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        val bytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        return try {
            decodePreferences(bytes) { Crypto.decrypt(it) } ?: UserPreferences()
        } catch (e: CorruptionException) {
            // Only after the user chose "Restore from seed", and after the old file was copied aside.
            if (UserPreferencesRepository.recoveryMode) UserPreferences() else throw e
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }
}

object UserPreferencesRepository {

    // initialized once with Application Context
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** True once the user chose to restore from seed after the stored data became unreadable. */
    @Volatile
    var recoveryMode: Boolean = false
        private set

    /**
     * Keeps a copy of the unreadable file next to the original, then lets the next write start from
     * an empty record. Returns false (and changes nothing) when the copy could not be made.
     */
    fun beginRecovery(): Boolean {
        return try {
            val file = appContext.dataStoreFile("user-preferences")
            if (file.exists() && file.length() > 0) {
                val copy = File(file.parentFile, "user-preferences.unreadable-${System.currentTimeMillis()}")
                file.copyTo(copy, overwrite = false)
            }
            recoveryMode = true
            true
        } catch (e: Exception) {
            false
        }
    }

    private val dataStore: DataStore<UserPreferences> by lazy {
        appContext.userPreferencesDataStore
    }

    val userPreferencesFlow: DataStore<UserPreferences>
        get() = dataStore

}
