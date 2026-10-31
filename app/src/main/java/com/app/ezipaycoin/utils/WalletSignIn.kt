package com.app.ezipaycoin.utils

import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException

/** Nonce, sign, login, with every failure turned into a message instead of being dropped. */
class WalletSignIn(private val repository: AuthRepository) {

    sealed class Result {
        data class Success(val nonce: String, val login: BaseResponse<LoginResponse>) : Result()
        data class Failed(val message: String) : Result()
    }

    suspend fun run(address: String, fcmToken: String, sign: (nonce: String) -> String): Result {
        return try {
            val nonceRes = repository.getNonce(address)
            if (!nonceRes.apiStatus) return Result.Failed(messageOrDefault(nonceRes.apiMessage))
            val signature = sign(nonceRes.apiData.nonce)
            val loginRes = repository.postLogin(LoginRequest(address, signature, fcmToken))
            if (!loginRes.apiStatus) return Result.Failed(messageOrDefault(loginRes.apiMessage))
            Result.Success(nonceRes.apiData.nonce, loginRes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Failed(messageOrDefault(e.localizedMessage))
        }
    }

    private fun messageOrDefault(message: String?): String =
        message?.takeIf { it.isNotBlank() } ?: "Unable to sign in. Please try again."
}
