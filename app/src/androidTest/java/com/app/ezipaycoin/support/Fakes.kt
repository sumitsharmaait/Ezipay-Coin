package com.app.ezipaycoin.support

import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.data.remote.dto.response.LoginResponse
import com.app.ezipaycoin.data.remote.dto.response.MeProfileResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionReceiptResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.domain.repository.HomeRepository
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.domain.repository.UserProfileRepository
import okhttp3.MultipartBody
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/** Sample data shared by the UI tests. Addresses are public test values. */
object Sample {
    const val OWN = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
    const val OTHER = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359"
    const val FAKE_KEY = "1111111111111111111111111111111111111111111111111111111111111111"

    fun coin(symbol: String, balance: Double, fiat: Double, contract: String? = null) =
        DashboardResponse.Crypto(null, "", contract, symbol, balance, fiat)

    val dashboard = DashboardResponse(
        crypto = listOf(
            coin("EZPT", 1250.5, 62.525, "0xCe135658a2E4db9F503B7922E2429c37d9d21DFc"),
            coin("BNB", 0.25, 150.0),
        ),
        fiat = listOf(DashboardResponse.Fiat("USD", "212.525")),
        marketSnapshot = emptyList()
    )

    fun tx(id: String, type: String, amount: Double, to: String, from: String) = TransactionsResponse.TransactionsItem(
        id = id, chain = "BSC", type = type, symbol = "EZPT", amount = amount, fiatValue = "0",
        timestamp = "2026-10-01T10:15:30+00:00", status = "Success", to = to, from = from, nonce = "1",
        gas = "21000", gasPrice = "5000000000", gasUsed = "21000", cumulativeGasUsed = "21000"
    )

    val transactions = BaseResponse(
        true, "OK", "",
        TransactionsResponse(
            listOf(tx("1", "send", 12.5, OTHER, OWN), tx("2", "received", 0.1, OWN, OTHER)),
            page = 1, size = 10, total = 2
        )
    )
}

class FakeHomeRepository(var dashboard: DashboardResponse = Sample.dashboard) : HomeRepository {
    val calls = AtomicInteger()
    @Volatile var failure: Throwable? = null

    override suspend fun getDashboard(): BaseResponse<DashboardResponse> {
        calls.incrementAndGet()
        failure?.let { throw it }
        return BaseResponse(true, "OK", "", dashboard)
    }
}

open class FakeTransactionsRepository : TransactionsRepository {
    val historyRequests = mutableListOf<TransactionsRequest>()
    val broadcasts = AtomicInteger()
    @Volatile var gasError: String? = null
    @Volatile var gasLimitHex = "0x5208"
    @Volatile var gasPriceHex = "0x12a05f200"

    override suspend fun getTransactions(request: TransactionsRequest): BaseResponse<TransactionsResponse> {
        synchronized(historyRequests) { historyRequests += request }
        return Sample.transactions
    }

    override suspend fun payMoney(request: BitPayRequest): BnbChainResponse {
        broadcasts.incrementAndGet()
        return BnbChainResponse("2.0", "1", "0xabc", null)
    }

    override suspend fun getTransactionReceipt(request: BitPayRequest) =
        TransactionReceiptResponse("2.0", "1", TransactionReceiptResponse.ReceiptResult("0x1"), null)

    override suspend fun getTransactionCount(request: BitPayRequest): BnbChainResponse =
        BnbChainResponse("2.0", "1", if (request.method == "eth_gasPrice") gasPriceHex else "0x7", null)

    override suspend fun getEstimateGas(request: EstimateGasRequest): BnbChainResponse {
        val err = gasError
        return if (err != null) {
            BnbChainResponse("2.0", "1", null, BnbChainResponse.ErrorBnbChain(-32000.0, err))
        } else {
            BnbChainResponse("2.0", "1", gasLimitHex, null)
        }
    }
}

class FakeAuthRepository : AuthRepository {
    override suspend fun getNonce(key: String) = BaseResponse(true, "OK", "", NonceResponse(key, "nonce"))
    override suspend fun postLogin(request: LoginRequest): BaseResponse<LoginResponse> = throw IOException("offline")
    override suspend fun updateProfile(request: UpdateProfileRequest) = BaseResponse(true, "OK", "", "ok")
    override suspend fun logout() = BaseResponse(true, "OK", "", "ok")
    override suspend fun deleteAccount(idempotencyKey: String): BaseResponse<String?> = BaseResponse(true, "OK", "", null)
    override suspend fun updateImage(file: MultipartBody.Part) = BaseResponse(true, "OK", "", "https://img/x.png")
}

class FakeUserProfileRepository(var profile: MeProfileResponse? = null) : UserProfileRepository {
    override suspend fun getMe(): BaseResponse<MeProfileResponse> {
        val p = profile ?: throw IOException("offline")
        return BaseResponse(true, "OK", "", p)
    }
}
