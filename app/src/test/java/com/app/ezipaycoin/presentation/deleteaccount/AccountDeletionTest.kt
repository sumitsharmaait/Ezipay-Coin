package com.app.ezipaycoin.presentation.deleteaccount

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.utils.PasswordGate
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AccountDeletionTest {

    private val keys = mutableListOf<String>()
    private var wiped = 0
    private var counter = 0

    private fun reply(ok: Boolean, msg: String) = BaseResponse<String?>(ok, msg, "", null)

    private fun deletion(
        remote: suspend (String) -> BaseResponse<String?>,
        gate: PasswordGate = PasswordGate(maxAttempts = 3)
    ) = AccountDeletion(
        passwordMatches = { it == "pw123456" },
        gate = gate,
        deleteRemote = { k -> keys += k; remote(k) },
        wipeLocal = { wiped++ },
        newKey = { "key-${++counter}" }
    )

    @Test
    fun wrongPasswordCallsNothing() = runTest {
        val d = deletion({ reply(true, "Account deleted") })
        assertEquals(DeletionResult.WrongPassword(2), d.run("nope"))
        assertTrue(keys.isEmpty())
        assertEquals(0, wiped)
    }

    @Test
    fun successWipesDeviceAfterServerConfirms() = runTest {
        val d = deletion({ reply(true, "Account deleted") })
        assertEquals(DeletionResult.Deleted, d.run("pw123456"))
        assertEquals(1, wiped)
        assertEquals(listOf("key-1"), keys)
    }

    @Test
    fun serverRefusalKeepsTheWallet() = runTest {
        val d = deletion({ reply(false, "FEATURE_DISABLED") })
        val r = d.run("pw123456")
        assertEquals(DeletionResult.Failed("Account deletion is temporarily unavailable"), r)
        assertEquals(0, wiped)
    }

    @Test
    fun networkErrorKeepsTheWalletAndRetryReusesTheKey() = runTest {
        var fail = true
        val d = deletion({ if (fail) throw IOException("timeout") else reply(true, "Account deleted") })
        assertTrue(d.run("pw123456") is DeletionResult.Failed)
        assertEquals(0, wiped)
        fail = false
        assertEquals(DeletionResult.Deleted, d.run("pw123456"))
        assertEquals(listOf("key-1", "key-1"), keys)
        assertEquals(1, wiped)
    }

    @Test
    fun unauthorizedKeepsTheWallet() = runTest {
        val body = "UN001".toResponseBody("text/plain".toMediaType())
        val d = deletion({ throw HttpException(Response.error<String>(401, body)) })
        val r = d.run("pw123456")
        assertTrue(r is DeletionResult.Failed)
        assertEquals(0, wiped)
    }

    @Test
    fun lockoutStopsAttempts() = runTest {
        val d = deletion({ reply(true, "Account deleted") })
        d.run("x"); d.run("x")
        assertTrue(d.run("x") is DeletionResult.LockedOut)
        assertTrue(d.run("pw123456") is DeletionResult.LockedOut)
        assertTrue(keys.isEmpty())
    }
}
