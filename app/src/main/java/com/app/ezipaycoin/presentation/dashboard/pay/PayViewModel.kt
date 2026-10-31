package com.app.ezipaycoin.presentation.dashboard.pay

import androidx.lifecycle.ViewModel
import com.app.ezipaycoin.utils.UserMessages
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import com.app.ezipaycoin.utils.validation.AddressValidator
import com.app.ezipaycoin.utils.validation.CoinKind
import com.app.ezipaycoin.utils.validation.ReceiptPolling
import kotlinx.coroutines.CancellationException
import com.app.ezipaycoin.utils.validation.AmountValidator
import com.app.ezipaycoin.utils.validation.TxErrorMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger

class PayViewModel(
    private val repository: TransactionsRepository,
    private val signer: TxSigner = WalletCoreTxSigner()
) : ViewModel() {
    private val _uiState = MutableStateFlow(PayState())
    val uiState: StateFlow<PayState> = _uiState.asStateFlow()


    // Set while a payment is being prepared, signed or broadcast; blocks a second submission.
    private var paymentInFlight = false
    private var ownAddress: String? = null

    init {
        getTransactions()
        viewModelScope.launch {
            ownAddress = UserPreferencesRepository.userPreferencesFlow.data.first().walletAddress
        }
    }

    fun ownAddressOrNull(): String? = ownAddress

    fun onEvent(event: PayEvent) {
        when (event) {
            is PayEvent.PayAmount -> {
                if (paymentInFlight) return
                if (_uiState.value.pendingTxHash != null) {
                    _uiState.update {
                        it.copy(payMoneyResponse = ResponseState.Error("Your previous payment is still confirming. Please wait before sending again"))
                    }
                    return
                }
                if (validateForm(event)) prepare(event)
            }

            is PayEvent.ConfirmPayment -> confirm()

            is PayEvent.CancelConfirmation -> {
                request = null
                _uiState.update { it.copy(confirmation = null) }
            }

            is PayEvent.AmountChanged -> {
                _uiState.update {
                    it.copy(amount = event.amount, amountError = null)
                }
            }

            is PayEvent.ExpandCurrencyOptions -> {
                _uiState.update {
                    it.copy(isCurrencyOptionsExpanded = event.isExpand)
                }

            }

            is PayEvent.CurrentOptionChanged -> {
                _uiState.update {
                    it.copy(isCurrencyOptionsExpanded = false)
                }

            }

            is PayEvent.ToAddressChanged -> {
                _uiState.update {
                    it.copy(
                        toAddress = event.toAddress,
                        toAddressError = AddressValidator.validateWhileTyping(event.toAddress, ownAddress)
                    )
                }

            }

            is PayEvent.TabChanged -> {
                _uiState.update {
                    it.copy(selectedTab = event.tab)
                }

            }

            is PayEvent.UtilityItemChanged -> {
                _uiState.update {
                    it.copy(selectedUtility = event.utilityItem)
                }

            }

            is PayEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(payMoneyResponse = ResponseState.Idle)
                }
            }

            is PayEvent.QRSuccess -> {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        toAddress = event.qr,
                        toAddressError = AddressValidator.validateWhileTyping(event.qr, ownAddress)
                    )
                }
            }

            is PayEvent.QRRetry -> {
                _uiState.update {
                    it.copy(isScanning = true)
                }
            }

            is PayEvent.QRDispose -> {
                _uiState.update {
                    it.copy(isScanning = false)
                }
            }
        }
    }


    /** A payment the user has asked for, held while the confirmation dialog is open. */
    private class Request(
        val to: String,
        val amount: BigDecimal,
        val amountWei: BigInteger,
        val symbol: String,
        val kind: CoinKind,
        val tokenBalance: Double?,
        val bnbBalance: Double?
    )

    /** Chain parameters for one signing attempt. */
    private class Quote(val nonce: String, val gasPrice: String, val gasLimit: String, val feeWei: BigInteger)

    private class PayFailure(message: String) : Exception(message)

    private var request: Request? = null

    private fun prepare(event: PayEvent.PayAmount) {
        val state = _uiState.value
        val kind = CoinKind.resolve(
            event.crypto, event.contractAddress,
            WalletManager.EZPT_TOKEN_CONTRACT, WalletManager.USDT_TOKEN_CONTRACT
        )
        if (kind is CoinKind.Unsupported) {
            _uiState.update { it.copy(payMoneyResponse = ResponseState.Error("${event.crypto} cannot be sent from the app yet")) }
            return
        }
        val amount = AmountValidator.parse(state.amount) ?: return
        val req = Request(
            to = state.toAddress.trim(),
            amount = amount,
            amountWei = AmountValidator.toWei(amount),
            symbol = event.crypto,
            kind = kind,
            tokenBalance = event.tokenBalance,
            bnbBalance = event.bnbBalance
        )
        paymentInFlight = true
        _uiState.update { it.copy(payMoneyResponse = ResponseState.Loading) }
        viewModelScope.launch {
            try {
                val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
                val from = prefs.walletAddress
                if (from == null || prefs.walletPrivateKey == null) {
                    _uiState.update { it.copy(payMoneyResponse = ResponseState.Idle) }
                    return@launch
                }
                val quote = quote(req, from)
                request = req
                _uiState.update {
                    it.copy(
                        payMoneyResponse = ResponseState.Idle,
                        confirmation = PayConfirmation(
                            to = req.to,
                            amount = req.amount.toPlainString(),
                            symbol = req.symbol,
                            feeBnb = AmountValidator.weiToBnb(quote.feeWei).stripTrailingZeros().toPlainString()
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: PayFailure) {
                _uiState.update { it.copy(payMoneyResponse = ResponseState.Error(e.message ?: TxErrorMapper.GENERIC)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(payMoneyResponse = ResponseState.Error(TxErrorMapper.friendly(e))) }
            } finally {
                paymentInFlight = false
            }
        }
    }

    private fun confirm() {
        val req = request ?: return
        if (paymentInFlight) return
        paymentInFlight = true
        _uiState.update { it.copy(confirmation = null, payMoneyResponse = ResponseState.Loading) }
        viewModelScope.launch {
            try {
                send(req)
            } catch (e: CancellationException) {
                throw e
            } catch (e: PayFailure) {
                _uiState.update { it.copy(payMoneyResponse = ResponseState.Error(e.message ?: TxErrorMapper.GENERIC)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(payMoneyResponse = ResponseState.Error(TxErrorMapper.friendly(e))) }
            } finally {
                request = null
                paymentInFlight = false
            }
        }
    }

    /** Fetches nonce, gas price and gas limit, and checks the funds against the fee. */
    private suspend fun quote(req: Request, from: String): Quote {
        val nonce = repository.getTransactionCount(getNonceRequest(from))
        val gasPrice = repository.getTransactionCount(getGasPriceRequest())
        if (nonce.result == null || gasPrice.result == null) {
            throw PayFailure(TxErrorMapper.friendly(nonce.error?.message ?: gasPrice.error?.message ?: "Network error"))
        }
        val payload = (req.kind as? CoinKind.Token)?.let { encodeTransferPayload(req.to, req.amountWei) }
        val gas = repository.getEstimateGas(estimateRequest(from, req, payload))
        if (gas.result == null) {
            throw PayFailure(TxErrorMapper.friendly(gas.error?.message ?: "Gas limit error"))
        }
        val feeWei = BigInteger(gas.result.removePrefix("0x"), 16) * BigInteger(gasPrice.result.removePrefix("0x"), 16)
        val fundsError = AmountValidator.validateFunds(
            amount = req.amount,
            symbol = req.symbol,
            tokenBalance = req.tokenBalance?.let { BigDecimal(it.toString()) },
            bnbBalance = req.bnbBalance?.let { BigDecimal(it.toString()) },
            feeBnb = AmountValidator.weiToBnb(feeWei)
        )
        if (fundsError != null) throw PayFailure(fundsError)
        return Quote(nonce.result, gasPrice.result, gas.result, feeWei)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun send(req: Request) {
        val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
        val from = prefs.walletAddress
        val key = prefs.walletPrivateKey
        if (from == null || key == null) {
            _uiState.update { it.copy(payMoneyResponse = ResponseState.Idle) }
            return
        }
        // Nonce and fee are fetched again: they may have moved while the dialog was open.
        val quote = quote(req, from)
        val output = when (val kind = req.kind) {
            is CoinKind.Token -> signer.signToken(
                quote.nonce, key, kind.contract,
                encodeTransferPayload(req.to, req.amountWei), quote.gasLimit, quote.gasPrice
            )
            else -> signer.signNative(quote.nonce, key, quote.gasPrice, quote.gasLimit, req.to, req.amountWei)
        }
        if (!output.errorMessage.isNullOrBlank()) {
            throw PayFailure(TxErrorMapper.GENERIC)
        }
        transferPayment(output.encoded.toHexString())
    }

    private suspend fun transferPayment(signedTxHex: String) {
        val res = repository.payMoney(
            BitPayRequest(
                jsonrpc = "2.0",
                method = "eth_sendRawTransaction",
                params = listOf("0x$signedTxHex"),
                id = "1"
            )
        )
        val hash = res.result ?: throw PayFailure(TxErrorMapper.friendly(res.error?.message))

        for (wait in ReceiptPolling.schedule()) {
            delay(wait)
            when (receiptStatus(hash)) {
                true -> {
                    onMined(success = true)
                    return
                }
                false -> {
                    onMined(success = false)
                    return
                }
                null -> Unit
            }
        }

        // Broadcast but not mined yet: tell the user it is pending, block a second send, keep watching.
        _uiState.update {
            it.copy(
                pendingTxHash = hash,
                payMoneyResponse = ResponseState.Success(
                    "Payment sent and still confirming.\nDo not send it again. It will show in your history once confirmed.\n\nRef: ${hash.take(10)}...${hash.takeLast(6)}"
                )
            )
        }
        viewModelScope.launch {
            repeat(60) {
                delay(10_000)
                val status = receiptStatus(hash)
                if (status != null) {
                    _uiState.update { it.copy(pendingTxHash = null) }
                    getTransactions()
                    return@launch
                }
            }
            _uiState.update { it.copy(pendingTxHash = null) }
        }
    }

    /** true = mined OK, false = reverted, null = no receipt yet (or the lookup failed). */
    private suspend fun receiptStatus(hash: String): Boolean? = try {
        val receipt = repository.getTransactionReceipt(
            BitPayRequest(jsonrpc = "2.0", method = "eth_getTransactionReceipt", params = listOf(hash), id = "1")
        ).result
        when {
            receipt == null -> null
            receipt.status.equals("0x1", true) -> true
            receipt.status.equals("0x0", true) -> false
            else -> null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private fun onMined(success: Boolean) {
        _uiState.update {
            if (success) {
                it.copy(payMoneyResponse = ResponseState.Success("Transaction Successful."), amount = "", toAddress = "")
            } else {
                it.copy(payMoneyResponse = ResponseState.Error("Transaction Failed."))
            }
        }
        viewModelScope.launch {
            delay(3000)
            getTransactions()
        }
    }

    private fun getTransactions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    responseState = ResponseState.Loading,
                )
            }
            try {
                val transactionsRequest = TransactionsRequest()
                val res = repository.getTransactions(transactionsRequest)
                if (res.apiStatus) {
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Success(res),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Error(
                                res.apiMessage
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        responseState = ResponseState.Error(
                            UserMessages.of(e, "Unknown error")
                        )
                    )
                }
            }
        }

    }

    private fun encodeTransferPayload(recipient: String, amount: BigInteger): ByteArray {
        // Step 1: Function selector
        val selector = byteArrayOf(0xa9.toByte(), 0x05.toByte(), 0x9c.toByte(), 0xbb.toByte())

        // Step 2: Parse and pad recipient address
        val cleanAddress = recipient.lowercase().removePrefix("0x")
        val addressBytes = BigInteger(cleanAddress, 16).toByteArray().takeLast(20).toByteArray()
        val paddedAddress = ByteArray(32).apply {
            System.arraycopy(addressBytes, 0, this, 12, 20) // pad left with 12 zeros
        }

        // Step 3: Pad amount to 32 bytes
        val amountBytes = amount.toByteArray()
        val paddedAmount = ByteArray(32).apply {
            val start = 32 - amountBytes.size
            System.arraycopy(amountBytes, 0, this, start, amountBytes.size)
        }

        // Step 4: Concatenate
        return selector + paddedAddress + paddedAmount
    }

    private fun getNonceRequest(walletAddress: String): BitPayRequest {
        return BitPayRequest(
            jsonrpc = "2.0",
            method = "eth_getTransactionCount",
            params = listOf(walletAddress, "pending"),
            id = "1"
        )
    }

    private fun getGasPriceRequest(): BitPayRequest {
        return BitPayRequest(
            jsonrpc = "2.0",
            method = "eth_gasPrice",
            params = emptyList(),
            id = "1"
        )
    }

    private fun estimateRequest(from: String, req: Request, payload: ByteArray?): EstimateGasRequest {
        val native = req.kind is CoinKind.Native
        val to = (req.kind as? CoinKind.Token)?.contract ?: req.to
        return EstimateGasRequest(
            jsonrpc = "2.0",
            method = "eth_estimateGas",
            params = listOf(
                EstimateGasRequest.ParamObj(
                    from = from,
                    to = to,
                    value = if (native) "0x${req.amountWei.toString(16)}" else "0x0",
                    data = if (native || payload == null) "" else "0x${payload.toHexKey()}"
                )
            ),
            id = "1"
        )
    }

    private fun ByteArray.toHexKey(): String = joinToString("") { "%02x".format(it) }

    private fun validateForm(event: PayEvent.PayAmount): Boolean {
        val state = _uiState.value
        _uiState.update { it.copy(amountError = null, toAddressError = null) }

        val addressError = if (state.toAddress.isBlank()) {
            "Please enter the recipient address"
        } else {
            AddressValidator.validate(state.toAddress, ownAddress)
        }
        if (addressError != null) {
            _uiState.update { it.copy(toAddressError = addressError) }
            return false
        }

        val amountError = AmountValidator.validateInput(state.amount)
            ?: AmountValidator.validateFunds(
                amount = AmountValidator.parse(state.amount)!!,
                symbol = event.crypto,
                tokenBalance = event.tokenBalance?.let { BigDecimal(it.toString()) },
                bnbBalance = null,
                feeBnb = null
            )
        if (amountError != null) {
            _uiState.update { it.copy(amountError = amountError) }
            return false
        }
        return true
    }

}
