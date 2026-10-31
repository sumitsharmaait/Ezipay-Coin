package com.app.ezipaycoin.testutil

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionsResponse

object Fixtures {
    const val OWN_ADDRESS = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
    const val RECIPIENT = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359"
    const val PRIVATE_KEY = "1111111111111111111111111111111111111111111111111111111111111111"

    fun crypto(symbol: String, balance: Double, contract: String? = null, fiat: Double = 0.0) =
        DashboardResponse.Crypto(
            tokenId = 1, tokenLogo = "", contractAddress = contract,
            symbol = symbol, balance = balance, fiatValue = fiat
        )

    fun dashboard(vararg coins: DashboardResponse.Crypto) = DashboardResponse(
        crypto = coins.toList(),
        fiat = listOf(DashboardResponse.Fiat("USD", "12.34")),
        marketSnapshot = emptyList()
    )

    fun ok(data: DashboardResponse) = BaseResponse(true, "OK", "", data)

    fun txItem(id: String = "t1", type: String = "send", symbol: String = "EZPT", amount: Double = 1.5) =
        TransactionsResponse.TransactionsItem(
            id = id, chain = "BSC", type = type, symbol = symbol, amount = amount,
            fiatValue = "0.00", timestamp = "2026-10-01T10:15:30+00:00", status = "Success",
            to = RECIPIENT, from = OWN_ADDRESS, nonce = "1", gas = "21000", gasPrice = "5000000000",
            gasUsed = "21000", cumulativeGasUsed = "21000"
        )

    fun txPage(vararg items: TransactionsResponse.TransactionsItem) =
        BaseResponse(true, "OK", "", TransactionsResponse(items.toList(), 1, 2, items.size))
}
