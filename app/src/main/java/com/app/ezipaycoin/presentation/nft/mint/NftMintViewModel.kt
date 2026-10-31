package com.app.ezipaycoin.presentation.nft.mint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.domain.repository.NftRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NftMintViewModel(
    private val repository: NftRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NftMintState())
    val state = _state.asStateFlow()

    private val _navEvents = Channel<NftMintNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        onEvent(NftMintEvent.Load)
    }

    fun onEvent(event: NftMintEvent) {
        when (event) {
            NftMintEvent.Load -> loadCollection()

            NftMintEvent.IncrementQuantity -> _state.update {
                if (it.canIncrement) it.copy(quantity = it.quantity + 1) else it
            }

            NftMintEvent.DecrementQuantity -> _state.update {
                if (it.canDecrement) it.copy(quantity = it.quantity - 1) else it
            }

            is NftMintEvent.QuantityChanged -> _state.update {
                it.copy(quantity = event.quantity.coerceIn(1, it.maxPerWallet))
            }

            is NftMintEvent.ProofLoaded -> _state.update { it.copy(proof = event.proof) }

            NftMintEvent.MintClicked -> mint()

            NftMintEvent.ErrorDismissed -> _state.update { it.copy(error = null) }

            NftMintEvent.SuccessAcknowledged -> viewModelScope.launch {
                _navEvents.send(NftMintNavEvent.ToHoldings(_state.value.transactionHash))
            }

            NftMintEvent.BackClicked -> viewModelScope.launch {
                _navEvents.send(NftMintNavEvent.Back)
            }
        }
    }

    private fun loadCollection() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        when (val result = repository.getCollection()) {
            is ResponseState.Success -> _state.update {
                it.copy(
                    isLoading = false,
                    collection = result.data,
                    phase = SalePhase.from(result.data.salePhase)
                )
            }

            is ResponseState.Error -> _state.update {
                it.copy(isLoading = false, error = result.message)
            }

            else -> Unit
        }
    }

    private fun mint() {
        val current = _state.value
        if (!current.canMint) return

        viewModelScope.launch {
            _state.update { it.copy(isMinting = true, error = null) }

            val result = when (current.phase) {
                SalePhase.PUBLIC -> repository.mintPublic(current.quantity)
                SalePhase.ALLOWLIST -> repository.mintAllowlist(current.quantity, current.proof)
                SalePhase.CLOSED -> return@launch
            }

            when (result) {
                is ResponseState.Success -> _state.update {
                    it.copy(
                        isMinting = false,
                        transactionHash = result.data.transactionHash
                    )
                }

                is ResponseState.Error -> _state.update {
                    it.copy(isMinting = false, error = result.message)
                }

                else -> _state.update { it.copy(isMinting = false) }
            }
        }
    }
}
