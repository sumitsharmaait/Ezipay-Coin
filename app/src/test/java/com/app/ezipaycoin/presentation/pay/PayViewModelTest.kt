package com.app.ezipaycoin.presentation.pay

import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.response.BnbChainResponse
import com.app.ezipaycoin.data.remote.dto.response.TransactionReceiptResponse
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.presentation.dashboard.pay.PayEvent
import com.app.ezipaycoin.presentation.dashboard.pay.PayViewModel
import com.app.ezipaycoin.testutil.FakeDataStore
import com.app.ezipaycoin.testutil.Fixtures
import com.app.ezipaycoin.testutil.MainDispatcherRule
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import com.app.ezipaycoin.utils.validation.AddressValidator
import com.app.ezipaycoin.utils.validation.AmountValidator
import com.app.ezipaycoin.utils.validation.TxErrorMapper
import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Ethereum
import java.io.IOException
import java.math.BigInteger

/**
 * Send flow: validate -> quote -> confirm -> sign -> broadcast -> receipt polling.
 * The chain RPC is a MockK repository; signing is replaced because the wallet-core native
 * library does not load on the JVM. Time is virtual (MainDispatcherRule).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PayViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val repo = mockk<TransactionsRepository>()
    private val signedInputs = mutableListOf<Ethereum.SigningInput>()
    private var signerError: String = ""
    private val rawTx = byteArrayOf(0x02, 0x7f, 0x0a)

    private val gasPriceHex = "0x12a05f200" // 5 gwei
    private val gasLimitHex = "0x5208" // 21000
    private val hash = "0xabcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"

    // Requests seen by the fake chain.
    private val rpcRequests = mutableListOf<BitPayRequest>()
    private val gasRequests = mutableListOf<EstimateGasRequest>()

    @Before
    fun setUp() {
        FakeDataStore.install(
            FakeDataStore(UserPreferences(walletAddress = Fixtures.OWN_ADDRESS, walletPrivateKey = Fixtures.PRIVATE_KEY))
        )
        mockkStatic(AnySigner::class)
        every { AnySigner.sign(any<MessageLite>(), any<CoinType>(), any<com.google.protobuf.Parser<Ethereum.SigningOutput>>()) } answers {
            signedInputs += firstArg<MessageLite>() as Ethereum.SigningInput
            Ethereum.SigningOutput.newBuilder()
                .setEncoded(ByteString.copyFrom(rawTx))
                .setErrorMessage(signerError)
                .build()
        }
        coEvery { repo.getTransactions(any<TransactionsRequest>()) } returns Fixtures.txPage(Fixtures.txItem())
        coEvery { repo.getTransactionCount(any()) } answers {
            val req = firstArg<BitPayRequest>()
            rpcRequests += req
            when (req.method) {
                "eth_getTransactionCount" -> rpc("0x7")
                "eth_gasPrice" -> rpc(gasPriceHex)
                else -> error("unexpected ${req.method}")
            }
        }
        coEvery { repo.getEstimateGas(any()) } answers {
            gasRequests += firstArg<EstimateGasRequest>()
            rpc(gasLimitHex)
        }
        coEvery { repo.payMoney(any()) } returns rpc(hash)
        coEvery { repo.getTransactionReceipt(any()) } returns receipt("0x1")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun rpc(result: String?, error: String? = null) =
        BnbChainResponse("2.0", "1", result, error?.let { BnbChainResponse.ErrorBnbChain(-32000.0, it) })

    private fun receipt(status: String?) =
        TransactionReceiptResponse("2.0", "1", status?.let { TransactionReceiptResponse.ReceiptResult(it) }, null)

    private fun vm(): PayViewModel = PayViewModel(repo).also { main.scheduler.runCurrent() }

    private fun PayViewModel.fill(to: String = Fixtures.RECIPIENT, amount: String = "1.5") {
        onEvent(PayEvent.ToAddressChanged(to))
        onEvent(PayEvent.AmountChanged(amount))
    }

    private fun PayViewModel.sendBnb(balance: Double? = 10.0, bnb: Double? = 10.0) {
        onEvent(PayEvent.PayAmount("BNB", tokenBalance = balance, bnbBalance = bnb))
        main.scheduler.runCurrent()
    }

    private fun PayViewModel.sendEzpt(balance: Double? = 100.0, bnb: Double? = 1.0, contract: String? = null) {
        onEvent(PayEvent.PayAmount("EZPT", tokenBalance = balance, bnbBalance = bnb, contractAddress = contract))
        main.scheduler.runCurrent()
    }

    // ---- loading ----

    @Test
    fun loadsTransactionHistoryOnInit() {
        val vm = vm()
        val state = vm.uiState.value.responseState
        assertTrue(state is ResponseState.Success)
        coVerify(exactly = 1) { repo.getTransactions(any()) }
    }

    @Test
    fun historyFailureIsShownNotThrown() {
        coEvery { repo.getTransactions(any()) } throws IOException("boom")
        val vm = vm()
        assertEquals(ResponseState.Error("boom"), vm.uiState.value.responseState)
    }

    // ---- validation ----

    @Test
    fun emptyAddressIsRejectedBeforeAnyNetworkCall() {
        val vm = vm()
        vm.onEvent(PayEvent.AmountChanged("1"))
        vm.sendBnb()
        assertEquals("Please enter the recipient address", vm.uiState.value.toAddressError)
        coVerify(exactly = 0) { repo.getEstimateGas(any()) }
    }

    @Test
    fun malformedAddressIsRejected() {
        val vm = vm()
        vm.fill(to = "0x1234")
        vm.sendBnb()
        assertEquals(AddressValidator.FORMAT_ERROR, vm.uiState.value.toAddressError)
        assertNull(vm.uiState.value.confirmation)
    }

    @Test
    fun badEip55ChecksumIsRejected() {
        val vm = vm()
        // Valid address with one letter's case flipped.
        vm.fill(to = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359".replace("fB69", "Fb69"))
        vm.sendBnb()
        assertEquals(AddressValidator.CHECKSUM_ERROR, vm.uiState.value.toAddressError)
    }

    @Test
    fun sendingToOwnAddressIsRejectedEvenInDifferentCase() {
        val vm = vm()
        vm.fill(to = Fixtures.OWN_ADDRESS.lowercase())
        vm.sendBnb()
        assertEquals(AddressValidator.OWN_ADDRESS_ERROR, vm.uiState.value.toAddressError)
    }

    @Test
    fun typingAnAddressValidatesLive() {
        val vm = vm()
        vm.onEvent(PayEvent.ToAddressChanged("hello"))
        assertEquals(AddressValidator.FORMAT_ERROR, vm.uiState.value.toAddressError)
        vm.onEvent(PayEvent.ToAddressChanged("0xfB69"))
        assertNull(vm.uiState.value.toAddressError)
        vm.onEvent(PayEvent.ToAddressChanged(Fixtures.OWN_ADDRESS))
        assertEquals(AddressValidator.OWN_ADDRESS_ERROR, vm.uiState.value.toAddressError)
    }

    @Test
    fun qrScanFillsTheAddressAndStopsScanning() {
        val vm = vm()
        vm.onEvent(PayEvent.QRRetry)
        assertTrue(vm.uiState.value.isScanning)
        vm.onEvent(PayEvent.QRSuccess(Fixtures.RECIPIENT))
        assertEquals(Fixtures.RECIPIENT, vm.uiState.value.toAddress)
        assertTrue(!vm.uiState.value.isScanning)
        assertNull(vm.uiState.value.toAddressError)
    }

    @Test
    fun invalidAmountsAreRejected() {
        val vm = vm()
        listOf("", "abc", "0", "-5", "1e3", "0.1234567890123456789").forEach { amount ->
            vm.fill(amount = amount)
            vm.sendBnb()
            assertNotNull("amount '$amount' should be rejected", vm.uiState.value.amountError)
            assertNull(vm.uiState.value.confirmation)
        }
        coVerify(exactly = 0) { repo.getEstimateGas(any()) }
    }

    @Test
    fun amountAboveBalanceIsRejected() {
        val vm = vm()
        vm.fill(amount = "100.01")
        vm.sendEzpt(balance = 100.0)
        assertEquals(AmountValidator.INSUFFICIENT_BALANCE, vm.uiState.value.amountError)
        coVerify(exactly = 0) { repo.getTransactionCount(any()) }
    }

    @Test
    fun editingTheAmountClearsItsError() {
        val vm = vm()
        vm.fill(amount = "0")
        vm.sendBnb()
        assertNotNull(vm.uiState.value.amountError)
        vm.onEvent(PayEvent.AmountChanged("1"))
        assertNull(vm.uiState.value.amountError)
    }

    @Test
    fun unknownCoinIsNotSentToAGuessedContract() {
        val vm = vm()
        vm.fill()
        vm.onEvent(PayEvent.PayAmount("DOGE", tokenBalance = 50.0, bnbBalance = 1.0))
        main.scheduler.runCurrent()
        assertEquals(ResponseState.Error("DOGE cannot be sent from the app yet"), vm.uiState.value.payMoneyResponse)
        coVerify(exactly = 0) { repo.getEstimateGas(any()) }
    }

    @Test
    fun withoutAWalletNothingHappens() {
        FakeDataStore.install(FakeDataStore(UserPreferences()))
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        assertEquals(ResponseState.Idle, vm.uiState.value.payMoneyResponse)
        assertNull(vm.uiState.value.confirmation)
    }

    // ---- quote and confirmation ----

    @Test
    fun validBnbRequestStopsAtConfirmationWithTheFee() {
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        val c = vm.uiState.value.confirmation
        assertNotNull(c)
        assertEquals(Fixtures.RECIPIENT, c!!.to)
        assertEquals("1.5", c.amount)
        assertEquals("BNB", c.symbol)
        assertEquals("0.000105", c.feeBnb) // 21000 gas * 5 gwei
        assertEquals(ResponseState.Idle, vm.uiState.value.payMoneyResponse)
        assertTrue("nothing may be signed or broadcast before the user confirms", signedInputs.isEmpty())
        coVerify(exactly = 0) { repo.payMoney(any()) }
    }

    @Test
    fun nativeEstimateCarriesValueAndNoCalldata() {
        val vm = vm()
        vm.fill(amount = "1.5")
        vm.sendBnb()
        val p = gasRequests.single().params.single()
        assertEquals(Fixtures.OWN_ADDRESS, p.from)
        assertEquals(Fixtures.RECIPIENT, p.to)
        assertEquals("0x14d1120d7b160000", p.value) // 1.5e18 wei
        assertEquals("", p.data)
    }

    @Test
    fun tokenEstimateTargetsTheContractWithErc20TransferCalldata() {
        val vm = vm()
        vm.fill(to = Fixtures.RECIPIENT, amount = "1.5")
        vm.sendEzpt()
        val p = gasRequests.single().params.single()
        assertEquals(WalletManager.EZPT_TOKEN_CONTRACT, p.to)
        assertEquals("0x0", p.value)
        // transfer(address,uint256): selector, left-padded recipient, 1.5e18 as uint256.
        assertEquals(
            "0xa9059cbb" +
                "000000000000000000000000fb6916095ca1df60bb79ce92ce3ea74c37c5d359" +
                "00000000000000000000000000000000000000000000000014d1120d7b160000",
            p.data
        )
    }

    @Test
    fun dashboardContractIsUsedForTheTokenWhenListed() {
        val listed = "0x55d398326f99059fF775485246999027B3197955"
        val vm = vm()
        vm.fill()
        vm.sendEzpt(contract = listed)
        assertEquals(listed, gasRequests.single().params.single().to)
    }

    @Test
    fun nonceIsReadAsPendingForTheSender() {
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        val nonce = rpcRequests.first { it.method == "eth_getTransactionCount" }
        assertEquals(listOf(Fixtures.OWN_ADDRESS, "pending"), nonce.params)
    }

    @Test
    fun feeOnTopOfAFullBnbBalanceIsRefused() {
        val vm = vm()
        vm.fill(amount = "10")
        vm.sendBnb(balance = 10.0, bnb = 10.0)
        assertEquals(
            ResponseState.Error("Insufficient balance after the network fee"),
            vm.uiState.value.payMoneyResponse
        )
        assertNull(vm.uiState.value.confirmation)
    }

    @Test
    fun tokenSendWithoutBnbForGasIsRefused() {
        val vm = vm()
        vm.fill(amount = "1")
        vm.sendEzpt(balance = 100.0, bnb = 0.00001)
        assertEquals(ResponseState.Error(AmountValidator.NO_FEE_BNB), vm.uiState.value.payMoneyResponse)
        assertNull(vm.uiState.value.confirmation)
    }

    @Test
    fun revertedEstimateShowsAFriendlyMessage() {
        coEvery { repo.getEstimateGas(any()) } returns rpc(null, "execution reverted: ERC20: transfer amount exceeds balance")
        val vm = vm()
        vm.fill()
        vm.sendEzpt()
        assertEquals(ResponseState.Error("Insufficient token balance"), vm.uiState.value.payMoneyResponse)
    }

    @Test
    fun missingNonceStopsTheFlow() {
        coEvery { repo.getTransactionCount(any()) } returns rpc(null, "nonce too low")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        assertTrue(vm.uiState.value.payMoneyResponse is ResponseState.Error)
        assertNull(vm.uiState.value.confirmation)
        coVerify(exactly = 0) { repo.getEstimateGas(any()) }
    }

    @Test
    fun networkFailureWhileQuotingIsMappedToAConnectionMessage() {
        coEvery { repo.getTransactionCount(any()) } throws IOException("unreachable")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        assertEquals(
            ResponseState.Error("No connection. Check your internet and try again"),
            vm.uiState.value.payMoneyResponse
        )
    }

    @Test
    fun canRetryAfterAFailedQuote() {
        coEvery { repo.getEstimateGas(any()) } returns rpc(null, "timeout") andThen rpc(gasLimitHex)
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        assertTrue(vm.uiState.value.payMoneyResponse is ResponseState.Error)
        vm.sendBnb()
        assertNotNull(vm.uiState.value.confirmation)
    }

    // ---- confirm -> sign -> broadcast ----

    @Test
    fun confirmSignsOnTheConfiguredChainAndBroadcastsTheSignedBytes() {
        val vm = vm()
        vm.fill(amount = "1.5")
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()

        val input = signedInputs.single()
        assertEquals(BigInteger.valueOf(WalletManager.CHAIN_ID), BigInteger(1, input.chainId.toByteArray()))
        assertEquals(97L, WalletManager.CHAIN_ID) // debug builds never sign for mainnet
        assertEquals(BigInteger.valueOf(7), BigInteger(1, input.nonce.toByteArray()))
        assertEquals(BigInteger("5000000000"), BigInteger(1, input.gasPrice.toByteArray()))
        assertEquals(BigInteger.valueOf(21000), BigInteger(1, input.gasLimit.toByteArray()))
        assertEquals(Fixtures.RECIPIENT, input.toAddress)
        assertEquals(BigInteger("1500000000000000000"), BigInteger(1, input.transaction.transfer.amount.toByteArray()))

        coVerify(exactly = 1) {
            repo.payMoney(match { it.method == "eth_sendRawTransaction" && it.params == listOf("0x027f0a") })
        }
    }

    @Test
    fun confirmFetchesAFreshNonceAndFee() {
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        // Two quotes, each reading nonce and gas price.
        assertEquals(2, rpcRequests.count { it.method == "eth_getTransactionCount" })
        assertEquals(2, rpcRequests.count { it.method == "eth_gasPrice" })
    }

    @Test
    fun minedSuccessClearsTheFormAndReloadsHistory() {
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        val s = vm.uiState.value
        assertEquals(ResponseState.Success("Transaction Successful."), s.payMoneyResponse)
        assertEquals("", s.amount)
        assertEquals("", s.toAddress)
        assertNull(s.pendingTxHash)
        assertNull(s.confirmation)
        coVerify(exactly = 2) { repo.getTransactions(any()) } // init + after success
    }

    @Test
    fun revertedTransactionShowsFailureAndKeepsTheForm() {
        coEvery { repo.getTransactionReceipt(any()) } returns receipt("0x0")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        assertEquals(ResponseState.Error("Transaction Failed."), vm.uiState.value.payMoneyResponse)
        assertEquals(Fixtures.RECIPIENT, vm.uiState.value.toAddress)
    }

    @Test
    fun signerErrorIsReportedAndNothingIsBroadcast() {
        signerError = "bad key"
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        assertEquals(ResponseState.Error(TxErrorMapper.GENERIC), vm.uiState.value.payMoneyResponse)
        coVerify(exactly = 0) { repo.payMoney(any()) }
    }

    @Test
    fun rejectedBroadcastShowsAFriendlyReason() {
        coEvery { repo.payMoney(any()) } returns rpc(null, "nonce too low")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        assertEquals(
            ResponseState.Error("Another transaction is still pending. Wait a moment and try again"),
            vm.uiState.value.payMoneyResponse
        )
        assertNull(vm.uiState.value.pendingTxHash)
    }

    @Test
    fun cancelDropsTheConfirmationAndConfirmThenDoesNothing() {
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.CancelConfirmation)
        assertNull(vm.uiState.value.confirmation)
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        assertTrue(signedInputs.isEmpty())
        coVerify(exactly = 0) { repo.payMoney(any()) }
    }

    @Test
    fun confirmWithoutARequestDoesNothing() {
        val vm = vm()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        coVerify(exactly = 0) { repo.payMoney(any()) }
    }

    // ---- double submit ----

    @Test
    fun secondPayWhileTheFirstIsPreparingIsIgnored() {
        val vm = vm()
        vm.fill()
        vm.onEvent(PayEvent.PayAmount("BNB", tokenBalance = 10.0, bnbBalance = 10.0))
        vm.onEvent(PayEvent.PayAmount("BNB", tokenBalance = 10.0, bnbBalance = 10.0))
        main.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repo.getEstimateGas(any()) }
    }

    @Test
    fun doubleTapOnConfirmBroadcastsOnce() {
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repo.payMoney(any()) }
        assertEquals(1, signedInputs.size)
    }

    // ---- slow confirmation ----

    @Test
    fun unminedPaymentIsReportedPendingAndBlocksASecondSend() {
        coEvery { repo.getTransactionReceipt(any()) } returns receipt(null)
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        // Run the whole receipt schedule (about a minute) but not the background watcher.
        main.scheduler.advanceTimeBy(65_000)
        main.scheduler.runCurrent()

        val s = vm.uiState.value
        assertEquals(hash, s.pendingTxHash)
        val msg = (s.payMoneyResponse as ResponseState.Success).data
        assertTrue(msg, msg.contains("still confirming"))
        assertTrue(msg, msg.contains("0xabcdef01...456789"))

        vm.onEvent(PayEvent.PayAmount("BNB", tokenBalance = 10.0, bnbBalance = 10.0))
        main.scheduler.runCurrent()
        val blocked = vm.uiState.value.payMoneyResponse as ResponseState.Error
        assertTrue(blocked.message, blocked.message.contains("still confirming"))
        coVerify(exactly = 1) { repo.payMoney(any()) }
    }

    @Test
    fun pendingPaymentIsReleasedOnceItsReceiptArrives() {
        coEvery { repo.getTransactionReceipt(any()) } returnsMany List(30) { receipt(null) } + receipt("0x1")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceTimeBy(65_000)
        main.scheduler.runCurrent()
        assertNotNull(vm.uiState.value.pendingTxHash)

        main.scheduler.advanceUntilIdle()
        assertNull(vm.uiState.value.pendingTxHash)
        coVerify(atLeast = 2) { repo.getTransactions(any()) }
    }

    @Test
    fun receiptLookupErrorsAreTreatedAsNotYetMined() {
        coEvery { repo.getTransactionReceipt(any()) } throws IOException("rpc down") andThen receipt("0x1")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        assertEquals(ResponseState.Success("Transaction Successful."), vm.uiState.value.payMoneyResponse)
    }

    @Test
    fun dismissDialogResetsTheResult() {
        coEvery { repo.getTransactionReceipt(any()) } returns receipt("0x0")
        val vm = vm()
        vm.fill()
        vm.sendBnb()
        vm.onEvent(PayEvent.ConfirmPayment)
        main.scheduler.advanceUntilIdle()
        vm.onEvent(PayEvent.DismissDialog)
        assertEquals(ResponseState.Idle, vm.uiState.value.payMoneyResponse)
    }
}
