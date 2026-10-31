package com.app.ezipaycoin.presentation.addtoken

import androidx.lifecycle.ViewModel
import com.app.ezipaycoin.utils.UserMessages
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.request.AddTokenRequest
import com.app.ezipaycoin.domain.repository.TokenRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTokenViewModel(
    private val repository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTokenState())
    val uiState: StateFlow<AddTokenState> = _uiState.asStateFlow()

    fun onEvent(event: AddTokenEvent) {
        when (event) {
            is AddTokenEvent.ContractAddressChanged -> {
                _uiState.update {
                    it.copy(
                        contractAddress = event.value.trim(),
                        // editing the address invalidates the previous auto-fill
                        autoFilled = false,
                        validateResponse = ResponseState.Idle
                    )
                }
                // auto-fetch as soon as a well-formed address is entered
                if (_uiState.value.isAddressValidFormat) {
                    validateAddress()
                }
            }

            is AddTokenEvent.ChainIdChanged -> _uiState.update { it.copy(chainId = event.value) }
            is AddTokenEvent.SymbolChanged -> _uiState.update { it.copy(symbol = event.value) }
            is AddTokenEvent.TokenNameChanged -> _uiState.update { it.copy(tokenName = event.value) }
            is AddTokenEvent.DecimalsChanged -> _uiState.update {
                it.copy(decimals = event.value.filter { c -> c.isDigit() })
            }

            is AddTokenEvent.ValidateAddress -> validateAddress()
            is AddTokenEvent.AddToken -> addToken()

            is AddTokenEvent.DismissDialog -> _uiState.update {
                when (event.which) {
                    0 -> it.copy(validateResponse = ResponseState.Idle)
                    1 -> it.copy(addResponse = ResponseState.Idle)
                    else -> it
                }
            }
        }
    }

    fun validateAddress() {
        val s = _uiState.value
        if (!s.isAddressValidFormat) return

        viewModelScope.launch {
            _uiState.update { it.copy(validateResponse = ResponseState.Loading) }
            try {
                val res = repository.validateToken(s.contractAddress, s.chainId)
                if (res.apiStatus && res.apiData.isValid) {
                    val meta = res.apiData
                    _uiState.update {
                        it.copy(
                            validateResponse = ResponseState.Success(meta),
                            // prefill, but keep editable (manual fallback)
                            symbol = meta.symbol.ifBlank { it.symbol },
                            tokenName = meta.tokenName.ifBlank { it.tokenName },
                            decimals = if (meta.decimals > 0) meta.decimals.toString() else it.decimals,
                            autoFilled = true
                        )
                    }
                } else {
                    // not fatal — user can still fill manually
                    _uiState.update {
                        it.copy(
                            validateResponse = ResponseState.Error(
                                res.apiMessage.ifBlank { "Couldn't auto-detect this token. Enter details manually." }
                            ),
                            autoFilled = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        validateResponse = ResponseState.Error(
                            UserMessages.of(e, "Couldn't auto-detect. Enter details manually.")
                        ),
                        autoFilled = false
                    )
                }
            }
        }
    }

    private fun addToken() {
        val s = _uiState.value
        val decimalsInt = s.decimals.toIntOrNull() ?: return
        if (!s.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(addResponse = ResponseState.Loading) }
            try {
                val req = AddTokenRequest(
                    ContractAddress = s.contractAddress,
                    ChainId = s.chainId,
                    Symbol = s.symbol.trim(),
                    TokenName = s.tokenName.trim(),
                    Decimals = decimalsInt,
                    TokenLogo = (s.validateResponse as? ResponseState.Success)?.data?.tokenLogo
                )
                val res = repository.addToken(req)
                if (res.apiStatus) {
                    _uiState.update { it.copy(addResponse = ResponseState.Success(res.apiData)) }
                } else {
                    _uiState.update { it.copy(addResponse = ResponseState.Error(res.apiMessage)) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(addResponse = ResponseState.Error(UserMessages.of(e, "Unknown error")))
                }
            }
        }
    }
}
