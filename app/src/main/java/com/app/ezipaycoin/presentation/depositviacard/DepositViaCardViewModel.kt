package com.app.ezipaycoin.presentation.depositviacard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.DepositeViaCardRequest
import com.app.ezipaycoin.data.remote.dto.request.NetworkInfoByTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.StatusDepositeRequest
import com.app.ezipaycoin.data.remote.dto.request.WalletInfoDepositeTokenRequest
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.domain.repository.DepositWithdrawalRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DepositViaCardViewModel(
    private val repository: DepositWithdrawalRepository,
    private val tokenId: DashboardResponse.Crypto
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepositeViaCardState())
    val uiState: StateFlow<DepositeViaCardState> = _uiState.asStateFlow()

    private var invoiceNumber: String = ""


    fun onEvent(event: DepositeViaCardEvent) {
        when (event) {
            is DepositeViaCardEvent.DismissDialog -> {
                _uiState.update {
                    when (event.which) {
                        1 -> it.copy(depositeResponse = ResponseState.Idle)
                        0 -> it.copy(walletInfoResponse = ResponseState.Idle)
                        else -> it // No change
                    }
                }
            }

            is DepositeViaCardEvent.OnAccountNameChanged -> {
                _uiState.update {
                    it.copy(accountName = event.name)
                }
            }

            is DepositeViaCardEvent.OnAmountChanged -> {
                _uiState.update {
                    it.copy(amount = event.amount)
                }
            }

            is DepositeViaCardEvent.OnCardNoChanged -> {
                _uiState.update {
                    it.copy(cardNo = event.cardNo)
                }
            }

            is DepositeViaCardEvent.OnCvvChanged -> {
                _uiState.update {
                    it.copy(cvv = event.cvv)
                }
            }

            is DepositeViaCardEvent.OnDepositSubmit -> {
                if (validateForm()) {
                    deposite()
                }
            }

            is DepositeViaCardEvent.OnExpiryMonthYearChanged -> {
                _uiState.update {
                    it.copy(expiryMonthYear = event.expiryMonthYear)
                }
            }

            is DepositeViaCardEvent.OnRemarksChanged -> {
                _uiState.update {
                    it.copy(remarks = event.remarks)
                }
            }
        }
    }


    init {
        viewModelScope.launch {
            val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
            prefs.walletAddress?.let { aVal ->
                _uiState.update {
                    it.copy(walletAddress = aVal)
                }
            }
        }
        getNetworkInfo()
    }

    private fun deposite() {

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    depositeResponse = ResponseState.Loading
                )
            }
            try {
                invoiceNumber = WalletManager.generateInvoiceNo()
                val req = DepositeViaCardRequest(
                    Remarks = _uiState.value.remarks,
                    InvoiceNumber = invoiceNumber,
                    BankAccountName = _uiState.value.accountName,
                    Amount = _uiState.value.amount,
                    CardNo = _uiState.value.cardNo.text.replace(" ", ""),
                    ExpiryMonth = _uiState.value.expiryMonthYear.text.split("/")[0].toInt(),
                    ExpiryYear = _uiState.value.expiryMonthYear.text.split("/")[1].toInt(),
                    CVV = _uiState.value.cvv
                )
                val transferPaymentRes = repository.depositeViaCard(req)
                if (transferPaymentRes.isSuccess) {
                    Log.e("transferPaymentRes", transferPaymentRes.toString())
                    transferPaymentRes.result.Url?.let { url ->
                        _uiState.update {
                            it.copy(depositeResponse = ResponseState.Success(
                                data = url
                            ))
                        }
                    } ?: run {
                        _uiState.update {
                            it.copy(
                                depositeResponse = ResponseState.Error(
                                    transferPaymentRes.result.Message ?: transferPaymentRes.message
                                )
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            depositeResponse = ResponseState.Error(
                                transferPaymentRes.result.Message ?: transferPaymentRes.message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        depositeResponse = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    private fun getNetworkInfo() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    walletInfoResponse = ResponseState.Loading,
                )
            }
            try {
                val res =
                    repository.getNetworkInfoByToken(NetworkInfoByTokenRequest(TokenId = tokenId.tokenId.toString()))
                if (res.isSuccess) {
                    invoiceNumber = WalletManager.generateInvoiceNo()
                    val req = WalletInfoDepositeTokenRequest(
                        InvoiceNumber = invoiceNumber,
                        NetworkValue = res.result[0].NetworkValue,
                        TokenValue = tokenId.symbol,
                        RecipientWalletAddressCrypto = _uiState.value.walletAddress
                    )
                    val walletInfoRes = repository.getWalletInfoDepositeToken(req)
                    if (walletInfoRes.isSuccess) {
                        walletInfoRes.result.WalletAddress?.let { address ->
                            _uiState.update {
                                it.copy(
                                    walletInfoResponse = ResponseState.Success(
                                        data = address
                                    ),
                                    networkInfo = res.result[0].NetworkText
                                )
                            }
                        } ?: {
                            _uiState.update {
                                it.copy(
                                    walletInfoResponse = ResponseState.Error(
                                        walletInfoRes.result.Message ?: "Unknown Error"
                                    ),
                                    networkInfo = res.result[0].NetworkText
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                walletInfoResponse = ResponseState.Error(
                                    walletInfoRes.message
                                )
                            )
                        }
                    }

                } else {
                    _uiState.update {
                        it.copy(
                            walletInfoResponse = ResponseState.Error(
                                res.message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        walletInfoResponse = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }


    private fun checkDepositeStatus() {
        viewModelScope.launch {
            while (true) {
                try {
                    val statusRes =
                        repository.checkDepositeStatus(StatusDepositeRequest(ClientReferenceNo = invoiceNumber))
                    if (statusRes.isSuccess) {
                        if (statusRes.result.RstKey == 1) {
                            _uiState.update {
                                it.copy(
                                    statusDepositeResponse = ResponseState.Success(
                                        data = 1
                                    )
                                )
                            }
                            break
                        } else if (statusRes.result.RstKey == 0) {
                            _uiState.update {
                                it.copy(
                                    statusDepositeResponse = ResponseState.Error(
                                        "Transaction Failed OR Not Found"
                                    )
                                )
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            statusDepositeResponse = ResponseState.Error(
                                e.localizedMessage ?: "Unknown error"
                            )
                        )
                    }
                }
                delay(5000)
            }
        }

    }


    private fun validateForm(): Boolean {

        _uiState.update {
            it.copy(
                cardNoError = "",
                expiryMonthYearError = "",
                cvvError = "",
                accountNameError = "",
                amountError = "",
                remarksError = ""
            )
        }

        if (_uiState.value.cardNo.text.replace(
                " ",
                ""
            ).length != 16
        ) {
            _uiState.update {
                it.copy(cardNoError = "Invalid Card Number")
            }
            return false
        }

        if (!_uiState.value.expiryMonthYear.text.matches(Regex("""(0[1-9]|1[0-2])/\d{2}"""))
        ) {
            _uiState.update {
                it.copy(expiryMonthYearError = "Invalid expiry date")
            }
            return false
        }

        if (_uiState.value.cvv.length < 3) {
            _uiState.update {
                it.copy(
                    cvvError = "Invalid CVV",
                )
            }
            return false
        }

        if (_uiState.value.accountName.isBlank()) {
            _uiState.update {
                it.copy(accountNameError = "Invalid Account Name")
            }
            return false
        }
        val amount = _uiState.value.amount.toDoubleOrNull()
        if (amount == null || amount < 1.0 || amount > 700.0) {
            _uiState.update {
                it.copy(
                    amountError = "Amount must be between 1 and 700"
                )
            }
            return false
        }

        if (_uiState.value.remarks.isBlank()) {
            _uiState.update {
                it.copy(remarksError = "Invalid Remarks")
            }
            return false
        }

        return true
    }


}