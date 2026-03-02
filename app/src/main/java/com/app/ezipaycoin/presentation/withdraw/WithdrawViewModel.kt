package com.app.ezipaycoin.presentation.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.request.NetworkInfoByTokenRequest
import com.app.ezipaycoin.data.remote.dto.request.WithdrawalTransferPayoutRequest
import com.app.ezipaycoin.data.remote.dto.response.DashboardResponse
import com.app.ezipaycoin.domain.repository.DepositWithdrawalRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WithdrawViewModel(
    private val repository: DepositWithdrawalRepository,
    private val tokenId: DashboardResponse.Crypto
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawState())
    val uiState: StateFlow<WithdrawState> = _uiState.asStateFlow()
    private var invoiceNumber: String = ""

    init {
        getNetworkInfo(tokenId.tokenId.toString())
    }


    fun onEvent(event: WithdrawEvent) {
        when (event) {
            is WithdrawEvent.DismissDialog -> {
                _uiState.update {
                    when (event.which) {
                        1 -> it.copy(transferInfoResponse = ResponseState.Idle)
                        0 -> it.copy(networkInfoResponse = ResponseState.Idle)
                        else -> it // No change
                    }
                }
            }

            is WithdrawEvent.WithdrawAmount -> {
                if (validateForm()) {
                    transferPayment()
                }
            }

            is WithdrawEvent.AmountChanged -> {
                _uiState.update {
                    it.copy(amount = event.amount)
                }
            }

            is WithdrawEvent.ToAddressChanged -> {
                _uiState.update {
                    it.copy(toAddress = event.toAddress)
                }
            }

            is WithdrawEvent.QRSuccess -> {
                _uiState.update {
                    it.copy(isScanning = false, toAddress = event.qr)
                }
            }

            is WithdrawEvent.QRRetry -> {
                _uiState.update {
                    it.copy(isScanning = true)
                }
            }

            is WithdrawEvent.QRDispose -> {
                _uiState.update {
                    it.copy(isScanning = false)
                }
            }
        }
    }

    private fun getNetworkInfo(tokenId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    networkInfoResponse = ResponseState.Loading,
                )
            }

            try {
                val res =
                    repository.getNetworkInfoByToken(NetworkInfoByTokenRequest(TokenId = tokenId))
                if (res.isSuccess) {
                    _uiState.update {
                        it.copy(
                            networkInfo = res.result[0].NetworkValue,
                            networkInfoResponse = ResponseState.Success(
                                data = res.result
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            networkInfoResponse = ResponseState.Error(
                                res.message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        networkInfoResponse = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    private fun transferPayment() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    transferInfoResponse = ResponseState.Loading,
                )
            }
            try {
                invoiceNumber = WalletManager.generateInvoiceNo()
                val req = WithdrawalTransferPayoutRequest(
                    InvoiceNumber = invoiceNumber,
                    NetworkValue = _uiState.value.networkInfo,
                    TokenValue = tokenId.symbol,
                    Towalletaddress = _uiState.value.toAddress,
                    Amount = _uiState.value.amount,
                    Contractaddress = tokenId.contractAddress.toString()
                )
                val transferPaymentRes = repository.withdrawalPayoutTransfer(req)
                if (transferPaymentRes.isSuccess) {
                    transferPaymentRes.result.TransactionId?.let { transaction ->
                        _uiState.update {
                            it.copy(
                                transferInfoResponse = ResponseState.Success(
                                    data = transaction
                                )
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            transferInfoResponse = ResponseState.Error(
                                transferPaymentRes.result.Message ?: transferPaymentRes.message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        transferInfoResponse = ResponseState.Error(
                            e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }


        }
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