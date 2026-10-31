package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.repository.AuthRepoImpl
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.SessionRejection
import com.app.ezipaycoin.utils.SessionRecovery
import com.app.ezipaycoin.utils.WalletManager
import com.app.ezipaycoin.utils.WalletSignIn
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import wallet.core.jni.PrivateKey
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * On a 401 with the body `UN001` (session replaced or expired) signs in again with the wallet stored on
 * the device and retries the request once. If that does not work the app is sent to the unlock screen;
 * the wallet is never removed.
 */
class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (!SessionRejection.shouldRecover(response.code, response.priorResponse != null, response.request.url.encodedPath) { bodyOf(response) }) {
            return null
        }
        val rejected = response.request.header("Authorization")?.removePrefix("Bearer ")
        val outcome = runBlocking { recovery.recover(rejected) }
        return when (outcome) {
            is SessionRecovery.Outcome.Retry ->
                response.request.newBuilder().header("Authorization", "Bearer ${outcome.token}").build()
            SessionRecovery.Outcome.GiveUp -> null
        }
    }

    private fun bodyOf(response: Response): String = try {
        response.peekBody(256).string()
    } catch (e: Exception) {
        ""
    }

    companion object {
        private val recovery = SessionRecovery(
            currentToken = { SessionManager.token },
            setToken = { SessionManager.token = it },
            relogin = { signInAgain() },
            onExpired = { SessionManager.notifySessionExpired() }
        )

        @OptIn(ExperimentalStdlibApi::class)
        private suspend fun signInAgain(): String? {
            val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
            val address = prefs.walletAddress
            val key = prefs.walletPrivateKey
            if (!prefs.isWalletCreated || address.isNullOrEmpty() || key.isNullOrEmpty()) return null
            val repository = AuthRepoImpl(ApiClient.authRetrofit.create(ApiService::class.java))
            val result = WalletSignIn(repository).run(address, fcmToken()) { nonce ->
                WalletManager.signNonceWithTrustWalletCore(nonce, PrivateKey(key.removePrefix("0x").hexToByteArray()))
            }
            if (result !is WalletSignIn.Result.Success) return null
            val token = result.login.apiData.token
            UserPreferencesRepository.userPreferencesFlow.updateData { it.copy(token = token) }
            return token
        }

        private fun fcmToken(): String {
            var token = ""
            val latch = CountDownLatch(1)
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) token = task.result.orEmpty()
                    latch.countDown()
                }
                latch.await(5, TimeUnit.SECONDS)
            } catch (e: Exception) {
                // Sign-in proceeds without a push token.
            }
            return token
        }
    }
}
