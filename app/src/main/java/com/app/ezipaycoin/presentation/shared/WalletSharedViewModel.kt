package com.app.ezipaycoin.presentation.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.domain.repository.HomeRepository
import com.app.ezipaycoin.domain.repository.UserDataRepository
import com.app.ezipaycoin.presentation.App
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletSharedViewModel(
    private val homeRepository: HomeRepository,
    private val dataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedState())
    val uiState: StateFlow<SharedState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SharedEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: SharedEvent) {
        when (event) {
            is SharedEvent.FetchBalance -> getDashboard()
            is SharedEvent.CryptoOptionChanged -> {
                _uiState.update {
                    it.copy(selectedCrypto = event.crypto)
                }
            }

            is SharedEvent.OpenUrl -> TODO()
        }
    }

    init {
        observeWalletState()
    }

    private fun observeWalletState() {
        viewModelScope.launch {
            dataRepository.isWalletCreate().collect { isCreated ->
                if (isCreated) {
                    _uiState.update {
                        it.copy(isRegistered = isCreated)
                    }
                    getDashboard()
                }
            }
        }
    }

    private fun getDashboard() {
        viewModelScope.launch {
            val prefs = App.getInstance().dataStore.data.first()
            SessionManager.token = prefs.token

            _uiState.update {
                it.copy(
                    responseState = ResponseState.Loading,
                )
            }
            try {
                val res = homeRepository.getDashboard()
                if (res.apiStatus) {
                    val selected = _uiState.value.selectedCrypto
                    val crypto = if (selected != null) {
                        res.apiData.crypto.firstOrNull {
                            it.symbol.equals(
                                selected.symbol,
                                ignoreCase = true
                            )
                        }
                    } else {
                        res.apiData.crypto.firstOrNull()
                    }
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Success(res),
                            selectedCrypto = crypto,
                            dashboardResponse = res.apiData
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

    fun onOpenLinkClicked(linkName: String) {
        val link: String = when (linkName) {
            "Facebook" -> "https://www.facebook.com/ezipaycoin/"
            "X" -> "https://x.com/EzipayCoin"
            "LinkedIn" -> "https://www.linkedin.com/company/ezipaycoin"
            "Instagram" -> "https://www.instagram.com/ezipaycoin/"
            "YouTube" -> "https://www.ezipaycoin.com/"
            "Terms & Conditions" -> "https://www.ezipaycoin.com/terms"
            "FAQ & Troubleshoot","FAQ & Help Center" -> "https://www.ezipaycoin.com/faq"
            "Telegram" -> "https://t.me/ezipaycoin"
            "Discord" -> "https://discord.com/invite/MRpZmUJP"
            else -> "https://www.ezipaycoin.com/"
        }
        viewModelScope.launch {
            _eventFlow.emit(SharedEvent.OpenUrl(link))
        }
    }

}