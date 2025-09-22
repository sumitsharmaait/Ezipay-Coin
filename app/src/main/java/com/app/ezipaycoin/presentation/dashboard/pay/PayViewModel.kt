package com.app.ezipaycoin.presentation.dashboard.pay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.request.BitPayRequest
import com.app.ezipaycoin.data.remote.dto.request.EstimateGasRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.domain.repository.TransactionsRepository
import com.app.ezipaycoin.presentation.App
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import com.google.protobuf.ByteString
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wallet.core.java.AnySigner
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Ethereum
import java.math.BigDecimal
import java.math.BigInteger

class PayViewModel(private val repository: TransactionsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PayState())
    val uiState: StateFlow<PayState> = _uiState.asStateFlow()


    init {
        getTransactions()
    }

    fun onEvent(event: PayEvent) {
        when (event) {
            is PayEvent.PayAmount -> {
                if (validateForm()) {
                    processPayment(event.crypto)
                    _uiState.update {
                        _uiState.value.copy(broadcastTransaction = true)
                    }

                }
            }

            is PayEvent.AmountChanged -> {
                _uiState.update {
                    it.copy(amount = event.amount)
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
                    it.copy(toAddress = event.toAddress)
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
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun processPayment(crypto: String) {
        viewModelScope.launch {
            val prefs = App.getInstance().dataStore.data.first()
            if (prefs.walletAddress == null || prefs.walletPrivateKey == null) {
                return@launch
            }
            _uiState.update {
                it.copy(
                    payMoneyResponse = ResponseState.Loading,
                )
            }
            try {
                val resNonce =
                    async { repository.getTransactionCount(getNonceRequest(prefs.walletAddress)) }.await()
                val resGasPrice =
                    async { repository.getTransactionCount(getGasPriceRequest()) }.await()

                if (null == resNonce.result || null == resGasPrice.result) {
                    var error = "Unknown Error"
                    error = resGasPrice.error?.message ?: "Gas Price Error"
                    error = resNonce.error?.message ?: "Nonce Error"
                    _uiState.update {
                        it.copy(
                            payMoneyResponse = ResponseState.Error(
                                error
                            )
                        )
                    }
                    return@launch
                }

                if (crypto.equals("BNB", true)) {
                    val resBnbGasLimit =
                        async {
                            repository.getEstimateGas(
                                getBnbGasEstimateRequest(
                                    prefs.walletAddress,
                                    crypto
                                )
                            )
                        }.await()

                    if (null == resBnbGasLimit.result) {
                        val msg =
                            if (resBnbGasLimit.error?.message?.contains("insufficient funds") == true) {
                                "Not enough BNB to cover network fees."
                            } else {
                                resBnbGasLimit.error?.message ?: "Gas Limit Error"
                            }
                        _uiState.update {
                            it.copy(
                                payMoneyResponse = ResponseState.Error(
                                    msg
                                )
                            )
                        }
                        return@launch
                    }

                    val output = getBNBSignedHex(
                        resNonce.result,
                        prefs.walletPrivateKey,
                        resGasPrice.result,
                        resBnbGasLimit.result
                    )
                    if (!output.errorMessage.isNullOrBlank()) {
                        _uiState.update {
                            it.copy(
                                payMoneyResponse = ResponseState.Error(
                                    output.errorMessage
                                )
                            )
                        }
                        return@launch
                    }
                    transferPayment(output.encoded.toByteArray().toHexString())

                } else {
                    val amount =
                        BigDecimal(_uiState.value.amount).multiply(BigDecimal("1000000000000000000"))
                            .toBigInteger()
                    val payload = encodeTransferPayload(_uiState.value.toAddress.trim(), amount)

                    val resBnbGasLimit =
                        async {
                            repository.getEstimateGas(
                                getBnbGasEstimateRequest(
                                    prefs.walletAddress,
                                    crypto,
                                    payload.toHexString()
                                )
                            )
                        }.await()

                    if (null == resBnbGasLimit.result) {
                        _uiState.update {
                            it.copy(
                                payMoneyResponse = ResponseState.Error(
                                    resBnbGasLimit.error?.message ?: "Gas Limit Error"
                                )
                            )
                        }
                        return@launch
                    }

                    val signedHex = signedHexTransferPaymentTokens(
                        resNonce.result,
                        prefs.walletPrivateKey,
                        crypto,
                        payload,
                        resBnbGasLimit.result,
                        resGasPrice.result
                    )
                    if (!signedHex.errorMessage.isNullOrBlank()) {
                        _uiState.update {
                            it.copy(
                                payMoneyResponse = ResponseState.Error(
                                    signedHex.errorMessage
                                )
                            )
                        }
                        return@launch
                    }
                    transferPayment(signedHex.encoded.toByteArray().toHexString())
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        payMoneyResponse = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
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
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }

    }

    private fun transferPayment(signedTxHex: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    payMoneyResponse = ResponseState.Loading,
                )
            }

            try {
                val payRequest = BitPayRequest(
                    jsonrpc = "2.0",
                    method = "eth_sendRawTransaction",
                    params = listOf("0x$signedTxHex"),
                    id = "1"
                )
                val res = repository.payMoney(payRequest)
                if (null != res.result) {
                    delay(3000)

                    val receiptRequest = BitPayRequest(
                        jsonrpc = "2.0",
                        method = "eth_getTransactionReceipt",
                        params = listOf(res.result),
                        id = "1"
                    )
                    val transactionReceipt = repository.getTransactionReceipt(receiptRequest)
                    if (null != transactionReceipt.result) {
                        if (transactionReceipt.result.status.equals("0x1", true)) {
                            _uiState.update {
                                it.copy(
                                    payMoneyResponse = ResponseState.Success("Transaction Successful."),
                                    amount = "",
                                    toAddress = ""
                                )
                            }
                        } else if (transactionReceipt.result.status.equals("0x0", true)) {
                            _uiState.update {
                                it.copy(
                                    payMoneyResponse = ResponseState.Error(
                                        "Transaction Failed."
                                    )
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                payMoneyResponse = ResponseState.Error(
                                    transactionReceipt.error?.message
                                        ?: "Transaction is not mined yet."
                                )
                            )
                        }
                    }
                    delay(3000)
                    getTransactions()
                } else {
                    _uiState.update {
                        it.copy(
                            payMoneyResponse = ResponseState.Error(
                                res.error?.message ?: "Unknown error"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        payMoneyResponse = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }

        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun signedHexTransferPaymentTokens(
        nonce: String,
        walletPrivateKey: String,
        crypto: String,
        payload: ByteArray,
        gasLimit: String,
        gasPrice: String
    ): Ethereum.SigningOutput {

        val contract = AnyAddress(
            if (crypto.equals(
                    "EZPT",
                    true
                )
            ) WalletManager.EZPT_TOKEN_CONTRACT else WalletManager.USDT_TOKEN_CONTRACT,
            CoinType.SMARTCHAIN
        )

        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(BigInteger.valueOf(97L).toByteArray())
            this.nonce =
                ByteString.copyFrom(BigInteger(nonce.removePrefix("0x"), 16).toByteArray())
            this.gasPrice = BigInteger(gasPrice.removePrefix("0x"), 16).toByteString()
            this.gasLimit = BigInteger(gasLimit.removePrefix("0x"), 16).toByteString()
            this.toAddress = contract.description()
            this.privateKey = ByteString.copyFrom(walletPrivateKey.hexToByteArray())
            transaction = Ethereum.Transaction.newBuilder().apply {
                contractGeneric = Ethereum.Transaction.ContractGeneric.newBuilder().apply {
                    this.data = ByteString.copyFrom(payload)
                    this.amount = ByteString.copyFrom(BigInteger.ZERO.toByteArray())
                }.build()
            }.build()
        }.build()

        val output = AnySigner.sign(input, CoinType.SMARTCHAIN, Ethereum.SigningOutput.parser())
        return output
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getBNBSignedHex(
        nonce: String,
        walletPrivateKey: String,
        gasPrice: String,
        gasLimit: String
    ): Ethereum.SigningOutput {

        val wei =
            (BigDecimal(_uiState.value.amount) * BigDecimal("1000000000000000000")).toBigInteger()
        val amountBytes = wei.toByteArray()
        val chainId = BigInteger.valueOf(97L).toByteArray()
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(chainId)
            this.nonce =
                ByteString.copyFrom(BigInteger(nonce.removePrefix("0x"), 16).toByteArray())
            this.gasPrice = BigInteger(gasPrice.removePrefix("0x"), 16).toByteString()
            this.gasLimit = BigInteger(gasLimit.removePrefix("0x"), 16).toByteString()
            this.toAddress = _uiState.value.toAddress.trim()
            this.transaction = Ethereum.Transaction.newBuilder().apply {
                this.transfer = Ethereum.Transaction.Transfer.newBuilder()
                    .setAmount(ByteString.copyFrom(amountBytes))
                    .build()
            }.build()
            this.privateKey = ByteString.copyFrom(walletPrivateKey.hexToByteArray())

        }.build()

        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        return output
    }


    private fun BigInteger.toByteString(): ByteString {
        return ByteString.copyFrom(this.toByteArray())
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

    private fun getBnbGasEstimateRequest(
        from: String,
        crypto: String,
        payload: String = ""
    ): EstimateGasRequest {
        val hexValue = BigDecimal(_uiState.value.amount).multiply(BigDecimal("1000000000000000000"))
            .toBigInteger()
        val to = if (crypto.equals("BNB", true)) {
            _uiState.value.toAddress.trim()
        } else if (crypto.equals("EZPT", true)) {
            WalletManager.EZPT_TOKEN_CONTRACT
        } else {
            WalletManager.USDT_TOKEN_CONTRACT
        }
        val params = listOf(
            EstimateGasRequest.ParamObj(
                from = from,
                to = to,
                value = if (crypto.equals("BNB", true)) "0x${hexValue.toString(16)}" else "0x0",
                data = if (crypto.equals("BNB", true)) "" else "0x${payload}"
            )
        )
        return EstimateGasRequest(
            jsonrpc = "2.0",
            method = "eth_estimateGas",
            params = params,
            id = "1"
        )
    }

    private fun validateForm(): Boolean {
        var isValid = true
        _uiState.update {
            _uiState.value.copy(amountError = null, toAddressError = null)
        }
        if (_uiState.value.toAddress.isBlank()) {
            _uiState.update {
                _uiState.value.copy(toAddressError = "Please Enter Sender Address")
            }
            isValid = false
        }

        if (_uiState.value.amount.isBlank()) {
            _uiState.update {
                _uiState.value.copy(amountError = "Please Enter Amount")
            }
            isValid = false
        }

        return isValid
    }


}