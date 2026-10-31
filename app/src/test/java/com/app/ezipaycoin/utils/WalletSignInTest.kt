package com.app.ezipaycoin.utils

import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletSignInTest {

    private class FakeAuth(
        val nonce: () -> BaseResponse<NonceResponse>,
        val login: () -> BaseResponse<LoginResponse>
    ) : AuthRepository {
        var logins = 0
        override suspend fun getNonce(key: String) = nonce()
        override suspend fun postLogin(request: LoginRequest): BaseResponse<LoginResponse> { logins++; return login() }
        override suspend fun updateProfile(request: UpdateProfileRequest): BaseResponse<String> = error("unused")
        override suspend fun logout(): BaseResponse<String> = error("unused")
        override suspend fun deleteAccount(idempotencyKey: String): BaseResponse<String?> = error("unused")
        override suspend fun updateImage(file: MultipartBody.Part): BaseResponse<String> = error("unused")
    }

    private fun nonceOk() = BaseResponse(true, "ok", "", NonceResponse("0xabc", "n-1"))
    private fun nonceFail(msg: String) = BaseResponse(false, msg, "", NonceResponse("", ""))
    private fun loginOk() = BaseResponse(true, "ok", "", LoginResponse("tok", LoginResponse.User("n", "e", null, "0xabc", "t", "k")))
    private fun loginFail(msg: String) = BaseResponse(false, msg, "", LoginResponse("", LoginResponse.User(null, null, null, "", "", "")))

    @Test
    fun successSignsTheServerNonce() = runTest {
        val signed = mutableListOf<String>()
        val r = WalletSignIn(FakeAuth({ nonceOk() }, { loginOk() })).run("0xabc", "") { signed += it; "sig" }
        assertEquals(listOf("n-1"), signed)
        assertEquals("tok", (r as WalletSignIn.Result.Success).login.apiData.token)
        assertEquals("n-1", r.nonce)
    }

    @Test
    fun failedNonceIsReportedAndLoginIsNotAttempted() = runTest {
        val repo = FakeAuth({ nonceFail("NONCE_DOWN") }, { loginOk() })
        val r = WalletSignIn(repo).run("0xabc", "") { "sig" }
        assertEquals(WalletSignIn.Result.Failed("NONCE_DOWN"), r)
        assertEquals(0, repo.logins)
    }

    @Test
    fun failedLoginIsReported() = runTest {
        val r = WalletSignIn(FakeAuth({ nonceOk() }, { loginFail("BAD_SIG") })).run("0xabc", "") { "sig" }
        assertEquals(WalletSignIn.Result.Failed("BAD_SIG"), r)
    }

    @Test
    fun blankMessageGetsADefault() = runTest {
        val r = WalletSignIn(FakeAuth({ nonceFail("") }, { loginOk() })).run("0xabc", "") { "sig" }
        assertTrue((r as WalletSignIn.Result.Failed).message.isNotBlank())
    }

    @Test
    fun networkErrorsBecomeFailures() = runTest {
        val r = WalletSignIn(FakeAuth({ throw java.io.IOException("offline") }, { loginOk() })).run("0xabc", "") { "sig" }
        assertEquals(WalletSignIn.Result.Failed("offline"), r)
    }
}
