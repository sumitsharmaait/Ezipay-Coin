package com.app.ezipaycoin.wallet

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.presentation.dashboard.pay.PayEvent
import com.app.ezipaycoin.presentation.dashboard.pay.PayViewModel
import com.app.ezipaycoin.support.FakeTransactionsRepository
import com.app.ezipaycoin.support.Sample
import com.app.ezipaycoin.support.ScreenTest
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Real wallet-core signing on a device, with a fake chain that records what would be broadcast.
 * The unit tests cannot load the native library, so this is where the signed bytes are checked.
 */
@RunWith(AndroidJUnit4::class)
class PaymentSigningTest : ScreenTest() {
    private val raw = CopyOnWriteArrayList<String>()

    private val chain = object : FakeTransactionsRepository() {
        override suspend fun payMoney(request: BitPayRequest): BnbChainResponse {
            raw += request.params.single()
            return super.payMoney(request)
        }
    }

    private fun send(coin: String, amount: String, contract: String? = null): PayViewModel {
        seed { it.copy(walletAddress = Sample.OWN, walletPrivateKey = Sample.FAKE_KEY, isWalletCreated = true) }
        val vm = onMain { PayViewModel(chain) }
        composeRule.runOnUiThread {
            vm.onEvent(PayEvent.ToAddressChanged(Sample.OTHER))
            vm.onEvent(PayEvent.AmountChanged(amount))
            vm.onEvent(PayEvent.PayAmount(coin, tokenBalance = 100.0, bnbBalance = 1.0, contractAddress = contract))
        }
        composeRule.waitUntil(5_000) { vm.uiState.value.confirmation != null }
        composeRule.runOnUiThread { vm.onEvent(PayEvent.ConfirmPayment) }
        composeRule.waitUntil(15_000) { raw.isNotEmpty() }
        composeRule.waitUntil(15_000) { vm.uiState.value.payMoneyResponse is ResponseState.Success }
        return vm
    }

    private val signatureTail = Regex("81e[56]a0[0-9a-f]{64}a0[0-9a-f]{64}$")

    @Test
    fun tokenTransferIsSignedAsAnErc20CallOnTheConfiguredChain() {
        send("EZPT", "1.5")
        val tx = raw.single()
        assertTrue(tx, tx.startsWith("0x"))
        val hex = tx.removePrefix("0x")
        // to = token contract, data = transfer(recipient, 1.5e18)
        assertTrue(hex, hex.contains(WalletManager.EZPT_TOKEN_CONTRACT.removePrefix("0x").lowercase()))
        assertTrue(
            hex,
            hex.contains(
                "a9059cbb" +
                    "000000000000000000000000fb6916095ca1df60bb79ce92ce3ea74c37c5d359" +
                    "00000000000000000000000000000000000000000000000014d1120d7b160000"
            )
        )
        assertEquals("debug builds sign for BSC testnet", 97L, WalletManager.CHAIN_ID)
        // EIP-155: v = chainId * 2 + 35 or 36 = 229 or 230 (0xe5, 0xe6)
        assertTrue(hex, signatureTail.containsMatchIn(hex))
    }

    @Test
    fun nativeTransferCarriesTheRecipientAndAmount() {
        send("BNB", "0.5")
        val hex = raw.single().removePrefix("0x")
        assertTrue(hex, hex.contains("fb6916095ca1df60bb79ce92ce3ea74c37c5d359"))
        assertTrue(hex, hex.contains("88" + "06f05b59d3b20000")) // 0.5e18 wei as an 8-byte RLP item
        assertTrue(hex, signatureTail.containsMatchIn(hex))
    }

    @Test
    fun signatureIsDeterministicForTheSameRequest() {
        send("BNB", "0.5")
        val first = raw.single()
        raw.clear()
        send("BNB", "0.5")
        assertEquals(first, raw.single())
    }
}
