package com.app.ezipaycoin.presentation.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.domain.repository.HomeRepository
import com.app.ezipaycoin.domain.repository.UserDataRepository
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.SessionManager
import com.app.ezipaycoin.utils.validation.RefreshThrottle
import kotlinx.coroutines.CancellationException
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
    private val dataRepository: UserDataRepository,
    clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedState())
    val uiState: StateFlow<SharedState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SharedEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Background refreshes (resume, push) are limited so quick tab or app switches do not hammer the API.
    private val resumeThrottle = RefreshThrottle(RESUME_REFRESH_MS, clock)
    private val pushThrottle = RefreshThrottle(PUSH_REFRESH_MS, clock)

    fun onEvent(event: SharedEvent) {
        when (event) {
            is SharedEvent.FetchBalance -> getDashboard()
            is SharedEvent.RefreshIfStale -> {
                if (_uiState.value.isUnlocked && resumeThrottle.tryAcquire()) getDashboard(silent = true)
            }
            is SharedEvent.PullRefresh -> {
                if (_uiState.value.isUnlocked) {
                    resumeThrottle.tryAcquire(force = true)
                    getDashboard(silent = true, pulled = true)
                }
            }
            is SharedEvent.CryptoOptionChanged -> {
                _uiState.update {
                    it.copy(selectedCrypto = event.crypto)
                }
            }

            is SharedEvent.AppUnlocked -> {
                _uiState.update {
                    it.copy(isUnlocked = true)
                }
                getDashboard()
            }

            is SharedEvent.AppLocked -> {
                _uiState.update { it.copy(isUnlocked = false) }
            }

            is SharedEvent.OpenUrl -> {
                viewModelScope.launch {
                    _eventFlow.emit(SharedEvent.OpenUrl(event.url))
                }
            }

            is SharedEvent.TransactionSelected -> {
                _uiState.update {
                    it.copy(selectedTransaction = event.transaction)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            SessionManager.transferEvents.collect {
                if (_uiState.value.isUnlocked && pushThrottle.tryAcquire()) {
                    resumeThrottle.tryAcquire(force = true)
                    getDashboard(silent = true)
                }
            }
        }
    }

//    private fun observeWalletState() {
//        viewModelScope.launch {
//            if (_uiState.value.isUnlocked){
//                dataRepository.isAuthenticated().collect { isCreated ->
//                    if (isCreated) {
//                        _uiState.update {
//                            it.copy(isUnlocked = true)
//                        }
//                        getDashboard()
//                    }
//                }
//            }
//        }
//    }

    /**
     * [silent] refreshes keep the current screen: no full-screen loader, and a failure leaves the
     * last good data in place. [pulled] drives the pull-to-refresh indicator.
     */
    private fun getDashboard(silent: Boolean = false, pulled: Boolean = false) {
        viewModelScope.launch {
            val prefs = UserPreferencesRepository.userPreferencesFlow.data.first()
            SessionManager.token = prefs.token

            val hasData = _uiState.value.dashboardResponse != null
            _uiState.update {
                if (silent && hasData) {
                    it.copy(isRefreshing = pulled)
                } else {
                    it.copy(responseState = ResponseState.Loading, isRefreshing = pulled)
                }
            }
            try {
                val res = homeRepository.getDashboard()
                if (res.apiStatus) {
                    val selected = _uiState.value.selectedCrypto
                    // Keep the user's coin when it is still listed, otherwise fall back to the first one
                    // (EZPT) so the balance card never ends up empty after a refresh.
                    val crypto = selected?.let { sel ->
                        res.apiData.crypto.firstOrNull { it.symbol.equals(sel.symbol, ignoreCase = true) }
                    } ?: res.apiData.crypto.firstOrNull()
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Success(res),
                            selectedCrypto = crypto,
                            dashboardResponse = res.apiData,
                            isRefreshing = false
                        )
                    }
                } else {
                    _uiState.update {
                        if (silent && hasData) it.copy(isRefreshing = false)
                        else it.copy(responseState = ResponseState.Error(res.apiMessage), isRefreshing = false)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    if (silent && hasData) it.copy(isRefreshing = false)
                    else it.copy(
                        responseState = ResponseState.Error(e.localizedMessage ?: "Unknown error"),
                        isRefreshing = false
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
            "YouTube", "Tutorial & Video", "Defi Basic Goals", "Searching Best Content" -> "https://www.youtube.com/@Ezipay_Coin/"
            "Terms & Conditions" -> "https://www.ezipaycoin.com/terms"
            "Support & Onboard" -> "https://www.ezipaycoin.com/contact"
            "FAQ & Troubleshoot", "FAQ & Help Center" -> "https://www.ezipaycoin.com/faq"
            "Whitepaper & Tokenomics" -> "https://www.ezipaycoin.com/whitepaper"
            "Telegram" -> "https://t.me/ezipaycoin"
            "Discord" -> "https://discord.com/invite/MRpZmUJP"
            "Privacy Policy" -> "https://ezipaycoin.notion.site/Ezipay-Coin-Wallet-Privacy-Policy-28f63db1841b8060a147dedeccbb5973"
            "YouTube Intro" -> "https://www.youtube.com/shorts/t5Pv3InTBnU"
            "YouTube Full" -> "https://vizay.in/video/Get-started.mp4"
            else -> "https://www.ezipaycoin.com/"
        }
        viewModelScope.launch {
            _eventFlow.emit(SharedEvent.OpenUrl(link))
        }
    }


    private companion object {
        const val RESUME_REFRESH_MS = 10_000L
        const val PUSH_REFRESH_MS = 2_000L
    }
}
