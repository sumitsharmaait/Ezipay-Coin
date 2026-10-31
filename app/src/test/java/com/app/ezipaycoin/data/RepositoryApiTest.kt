package com.app.ezipaycoin.data

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.repository.AuthRepoImpl
import com.app.ezipaycoin.data.repository.HomeRepoImpl
import com.app.ezipaycoin.data.repository.TransactionRepoImpl
import com.app.ezipaycoin.data.repository.UserProfileRepoImpl
import com.app.ezipaycoin.utils.SessionManager
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/**
 * Repositories against a MockWebServer: request line, headers and body on the way out,
 * envelope parsing (BaseResponse) on the way back. Golden JSON lives in src/test/resources/golden
 * and mirrors the shapes recorded in ezipay-coin-platform/contracts/golden.
 */
class RepositoryApiTest {
    private lateinit var h: ApiHarness

    @Before
    fun setUp() {
        h = ApiHarness()
        SessionManager.token = "jwt-123"
    }

    @After
    fun tearDown() {
        SessionManager.token = ""
        h.shutdown()
    }

    private fun json(text: String) = JsonParser.parseString(text).asJsonObject

    // ---- auth ----

    @Test
    fun nonceIsRequestedWithThePublicKeyQuery() = runBlocking {
        h.enqueueGolden("auth-nonce.json")
        val res = AuthRepoImpl(h.api).getNonce("0x706Dd65Ea2fd44Bc4a788BA96f09f0ded4f7110F")
        val req = h.server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/api/v1/Auth/nonce?publicKey=0x706Dd65Ea2fd44Bc4a788BA96f09f0ded4f7110F", req.path)
        assertTrue(res.apiStatus)
        assertEquals("yhNG78GJrN", res.apiData.nonce)
        assertEquals("0x706Dd65Ea2fd44Bc4a788BA96f09f0ded4f7110F", res.apiData.publicKey)
        assertNull(res.error)
    }

    @Test
    fun loginPostsTheSignedNonceAndParsesTheSession() = runBlocking {
        h.enqueueGolden("auth-login.json")
        val res = AuthRepoImpl(h.api).postLogin(LoginRequest("0xabc", "0xsig", "device-1"))
        val req = h.server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/v1/Auth/login", req.path)
        assertTrue(req.getHeader("Content-Type")!!.startsWith("application/json"))
        val body = json(req.body.readUtf8())
        assertEquals(setOf("publicKey", "signature", "deviceId"), body.keySet())
        assertEquals("0xsig", body["signature"].asString)
        assertEquals("device-1", body["deviceId"].asString)
        assertEquals("header.payload.signature", res.apiData.token)
        assertEquals("Contract Test", res.apiData.user.name)
        assertNull(res.apiData.user.profilePic)
        assertEquals("pending", res.apiData.user.kycStatus)
    }

    @Test
    fun logoutIsAGetWithTheBearerToken() = runBlocking {
        h.enqueueJson("""{"apiStatus":true,"apiMessage":"Logged out","error":null,"apiData":"ok"}""")
        val res = AuthRepoImpl(h.api).logout()
        val req = h.server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/api/v1/Auth/logout", req.path)
        assertEquals("Bearer jwt-123", req.getHeader("Authorization"))
        assertTrue(res.apiStatus)
    }

    @Test
    fun updateProfilePostsNameAndEmail() = runBlocking {
        h.enqueueJson("""{"apiStatus":true,"apiMessage":"Updated","error":null,"apiData":"ok"}""")
        AuthRepoImpl(h.api).updateProfile(UpdateProfileRequest("Raj", "raj@example.org"))
        val req = h.server.takeRequest()
        assertEquals("/api/v1/Users/UpdateProfile", req.path)
        assertEquals("Bearer jwt-123", req.getHeader("Authorization"))
        val body = json(req.body.readUtf8())
        assertEquals("Raj", body["name"].asString)
        assertEquals("raj@example.org", body["email"].asString)
    }

    // ---- bearer header ----

    @Test
    fun noAuthorizationHeaderWhenThereIsNoSession() = runBlocking {
        SessionManager.token = null
        h.enqueueGolden("auth-nonce.json")
        AuthRepoImpl(h.api).getNonce("0x1")
        assertNull(h.server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun anEmptyTokenStillSendsABearerHeader() = runBlocking {
        // SessionManager.token starts as "" so pre-login calls carry an empty Bearer header (OkHttp trims the space). Pinned as current behaviour.
        SessionManager.token = ""
        h.enqueueGolden("auth-nonce.json")
        AuthRepoImpl(h.api).getNonce("0x1")
        assertEquals("Bearer", h.server.takeRequest().getHeader("Authorization"))
    }

    // ---- dashboard ----

    @Test
    fun dashboardParsesBalancesAndHandlesNulls() = runBlocking {
        h.enqueueGolden("dashboard.json")
        val res = HomeRepoImpl(h.api).getDashboard()
        val req = h.server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/api/v1/dashboard", req.path)
        assertEquals("Bearer jwt-123", req.getHeader("Authorization"))

        assertTrue(res.apiStatus)
        val coins = res.apiData.crypto
        assertEquals(listOf("EZPT", "BNB", "USDT"), coins.map { it.symbol })
        assertEquals(1250.5, coins[0].balance, 0.0)
        assertEquals("1250.5000", coins[0].formattedBalance)
        assertEquals("0xCe135658a2E4db9F503B7922E2429c37d9d21DFc", coins[0].contractAddress)
        assertNull("BNB is native and has no contract", coins[1].contractAddress)
        assertNull(coins[0].tokenId)
        assertEquals(7, coins[2].tokenId)
        assertEquals("212.525", res.apiData.fiat.single().balance)
        assertEquals(-0.35, res.apiData.marketSnapshot.single().changePct, 0.0)
        assertEquals(3, res.apiData.marketSnapshot.single().sparkline.size)
    }

    @Test
    fun applicationErrorEnvelopeIsReturnedNotThrown() = runBlocking {
        h.enqueueJson("""{"apiStatus":false,"apiMessage":"Dashboard unavailable","error":"upstream timeout","apiData":null}""")
        val res = HomeRepoImpl(h.api).getDashboard()
        assertFalse(res.apiStatus)
        assertEquals("Dashboard unavailable", res.apiMessage)
        assertEquals("upstream timeout", res.error)
    }

    @Test
    fun featureDisabledIsAnEnvelopeWithNoData() = runBlocking {
        h.enqueueGolden("feature-disabled.json")
        val res = HomeRepoImpl(h.api).getDashboard()
        assertFalse(res.apiStatus)
        assertEquals("FEATURE_DISABLED", res.apiMessage)
        assertNull(res.error)
        // The envelope type says apiData is non-null; Gson stores null anyway. Callers must check apiStatus first.
        val data: Any? = res.apiData
        assertNull(data)
    }

    @Test
    fun unauthorizedResponseThrowsHttpException() {
        h.enqueueJson("Unauthorized", code = 401)
        try {
            runBlocking { HomeRepoImpl(h.api).getDashboard() }
            fail("expected HttpException")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }
    }

    // ---- profile ----

    @Test
    fun usersMeIsParsedAndUnknownFieldsAreIgnored() = runBlocking {
        h.enqueueGolden("users-me.json")
        val res = UserProfileRepoImpl(h.api).getMe()
        val req = h.server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/api/v1/Users/me", req.path)
        assertEquals("Bearer jwt-123", req.getHeader("Authorization"))
        assertTrue(res.apiStatus)
        assertEquals("Contract Test", res.apiData.name)
        assertEquals("probe@example.com", res.apiData.email)
        assertNull(res.apiData.profilePic)
        assertEquals("pending", res.apiData.kycStatus)
        assertEquals("0x706Dd65Ea2fd44Bc4a788BA96f09f0ded4f7110F", res.apiData.publicKey)
    }

    // ---- transactions ----

    @Test
    fun transactionsRequestBodyAndPaging() = runBlocking {
        h.enqueueGolden("wallet-transactions.json")
        val res = TransactionRepoImpl(h.api, h.rpc).getTransactions(TransactionsRequest())
        val req = h.server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/v1/Wallet/Transactions", req.path)
        assertEquals("Bearer jwt-123", req.getHeader("Authorization"))
        val body = json(req.body.readUtf8())
        assertEquals("BSC", body["chain"].asString)
        assertEquals("all", body["type"].asString)
        assertEquals(1, body["page"].asInt)
        assertEquals(2, body["size"].asInt)

        val items = res.apiData.items
        assertEquals(2, items.size)
        assertEquals("send", items[0].type)
        assertEquals("12.5000", items[0].formattedAmount)
        assertEquals("4", items[0].nonce)
        assertNull(items[1].nonce)
        assertEquals("October 1, 2026 | 10:15AM", items[0].formattedDate)
        assertEquals(2, res.apiData.total)
    }

    @Test
    fun upstreamFailureWithTextDataBreaksGsonParsing() {
        // The backend answers apiStatus=false with a plain string in apiData (see the recorded golden).
        // The typed model cannot hold it, so the call throws instead of returning the envelope.
        h.enqueueGolden("wallet-transactions-upstream-error.json")
        try {
            runBlocking { TransactionRepoImpl(h.api, h.rpc).getTransactions(TransactionsRequest()) }
            fail("expected a parse failure")
        } catch (e: JsonSyntaxException) {
            assertNotNull(e.message)
        }
    }

    // ---- chain RPC (JSON-RPC at the server root) ----

    @Test
    fun jsonRpcCallsGoToTheRootWithTheRpcBody() = runBlocking {
        h.enqueueGolden("rpc-result.json")
        val repo = TransactionRepoImpl(h.api, h.rpc)
        val res = repo.getTransactionCount(BitPayRequest(method = "eth_getTransactionCount", params = listOf("0xabc", "pending")))
        val req = h.server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/", req.path)
        assertNull("the RPC node must never receive the API token", req.getHeader("Authorization"))
        val body = json(req.body.readUtf8())
        assertEquals("2.0", body["jsonrpc"].asString)
        assertEquals("eth_getTransactionCount", body["method"].asString)
        assertEquals(listOf("0xabc", "pending"), body["params"].asJsonArray.map { it.asString })
        assertEquals("0x7", res.result)
        assertNull(res.error)
    }

    @Test
    fun rpcErrorIsParsed() = runBlocking {
        h.enqueueGolden("rpc-error.json")
        val res = TransactionRepoImpl(h.api, h.rpc).payMoney(BitPayRequest(method = "eth_sendRawTransaction", params = listOf("0x00")))
        assertNull(res.result)
        assertEquals("nonce too low", res.error?.message)
        assertEquals(-32000.0, res.error!!.code, 0.0)
    }

    @Test
    fun estimateGasBodyCarriesTheCallObject() = runBlocking {
        h.enqueueGolden("rpc-result.json")
        val call = EstimateGasRequest.ParamObj(from = "0xa", to = "0xb", value = "0x0", data = "0xa9059cbb")
        TransactionRepoImpl(h.api, h.rpc).getEstimateGas(EstimateGasRequest(method = "eth_estimateGas", params = listOf(call)))
        val p = json(h.server.takeRequest().body.readUtf8())["params"].asJsonArray[0].asJsonObject
        assertEquals("0xa", p["from"].asString)
        assertEquals("0xb", p["to"].asString)
        assertEquals("0x0", p["value"].asString)
        assertEquals("0xa9059cbb", p["data"].asString)
    }

    @Test
    fun receiptStatusAndPendingReceiptAreParsed() = runBlocking {
        val repo = TransactionRepoImpl(h.api, h.rpc)
        val q = BitPayRequest(method = "eth_getTransactionReceipt", params = listOf("0xabc"))
        h.enqueueGolden("rpc-receipt-success.json")
        assertEquals("0x1", repo.getTransactionReceipt(q).result?.status)
        h.enqueueGolden("rpc-receipt-pending.json")
        assertNull(repo.getTransactionReceipt(q).result)
    }
}
