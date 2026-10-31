package com.app.ezipaycoin.utils.validation

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Turns raw RPC, revert and network errors into messages a user can act on. */
object TxErrorMapper {
    const val GENERIC = "The payment could not be completed. Please try again"

    fun friendly(raw: String?): String {
        val m = raw?.lowercase().orEmpty()
        return when {
            m.isBlank() -> GENERIC
            "0xe450d38c" in m || "exceeds balance" in m || "insufficientbalance" in m ->
                "Insufficient token balance"
            "0xfb8f41b2" in m || "insufficientallowance" in m || "exceeds allowance" in m ->
                "Token allowance is too low for this payment"
            "0xec442f05" in m || "invalidreceiver" in m -> "This recipient address cannot receive the token"
            "insufficient funds" in m || "gas required exceeds" in m -> AmountValidator.NO_FEE_BNB
            "nonce too low" in m || "already known" in m || "replacement transaction underpriced" in m ->
                "Another transaction is still pending. Wait a moment and try again"
            "nonce too high" in m -> "Transaction order is out of sync. Please try again"
            "timeout" in m || "timed out" in m -> "The network took too long to respond. Please try again"
            "unable to resolve host" in m || "failed to connect" in m || "network is unreachable" in m ->
                "No connection. Check your internet and try again"
            "execution reverted" in m || "revert" in m -> "The network rejected this payment"
            "gas price" in m || "underpriced" in m -> "Network fee changed. Please try again"
            else -> GENERIC
        }
    }

    fun friendly(t: Throwable): String = when (t) {
        is SocketTimeoutException -> friendly("timeout")
        is UnknownHostException -> friendly("unable to resolve host")
        is IOException -> "No connection. Check your internet and try again"
        else -> if (com.app.ezipaycoin.utils.UserMessages.isParseError(t)) com.app.ezipaycoin.utils.UserMessages.UNEXPECTED_RESPONSE else friendly(t.message)
    }
}
