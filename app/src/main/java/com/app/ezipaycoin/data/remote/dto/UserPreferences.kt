package com.app.ezipaycoin.data.remote.dto

import androidx.datastore.core.Serializer
import com.app.ezipaycoin.utils.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    val userProfile: String? = null
)


object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        return try {
            val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
            val decryptedBytes = Crypto.decrypt(encryptedBytesDecoded)
            val decodedJsonString = decryptedBytes.decodeToString()
            return Json.decodeFromString(decodedJsonString)
        } catch (e: Exception) {
            UserPreferences()
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
