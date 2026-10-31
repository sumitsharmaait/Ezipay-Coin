package com.app.ezipaycoin.presentation.deposit

import com.app.ezipaycoin.data.remote.api.PaymentsProxy
import com.app.ezipaycoin.utils.UserMessages
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
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

class DepositViewModel(
    private val repository: DepositWithdrawalRepository,
    private val tokenId: DashboardResponse.Crypto
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepositeState())
    val uiState: StateFlow<DepositeState> = _uiState.asStateFlow()

    private var invoiceNumber: String = ""


    fun onEvent(event: DepositeEvent) {
        when (event) {
            is DepositeEvent.DismissDialog -> {
                _uiState.update {
                    when (event.which) {
                        1 -> it.copy(statusDepositeResponse = ResponseState.Idle)
                        0 -> it.copy(walletInfoResponse = ResponseState.Idle)
                        else -> it // No change
                    }
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
                        TokenValue = tokenId.symbol.orEmpty(),
                        RecipientWalletAddressCrypto = _uiState.value.walletAddress
                    )
                    val walletInfoRes = repository.getWalletInfoDepositeToken(req, PaymentsProxy.newIdempotencyKey())
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
                            delay(10000L)
                            checkDepositeStatus()
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
                            UserMessages.of(e, "Unknown error")
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
                                UserMessages.of(e, "Unknown error")
                            )
                        )
                    }
                }
                delay(5000)
            }
        }

    }


}