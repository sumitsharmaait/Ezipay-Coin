package com.app.ezipaycoin.presentation.nft

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

/**
 * @param ownerProvider reads the wallet address lazily. WalletManager holds the wallet
 *   in memory only, so the address can still be null when this ViewModel is
 *   constructed and become available a moment later — reading it at load time rather
 *   than at construction lets Refresh recover instead of caching a blank value.
 */
class NftViewModel(
    private val repository: NftRepository,
    private val ownerProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(NftState())
    val state = _state.asStateFlow()

    private val _navEvents = Channel<NftNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        onEvent(NftEvent.Load)
    }

    fun onEvent(event: NftEvent) {
        when (event) {
            NftEvent.Load -> loadHoldings(refresh = false)
            NftEvent.Refresh -> loadHoldings(refresh = true)
            NftEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
            NftEvent.MintClicked -> emit(NftNavEvent.ToMint)
            is NftEvent.NftClicked -> emit(NftNavEvent.ToDetail(event.tokenId))
        }
    }

    private fun loadHoldings(refresh: Boolean) {
        val owner = ownerProvider().orEmpty()

        if (owner.isBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    items = emptyList(),
                    error = "Unlock your wallet to see the NFTs you hold."
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    ownerAddress = owner,
                    error = null
                )
            }

            when (val result = repository.getHoldings(owner)) {
                is ResponseState.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = result.data.items.orEmpty()
                    )
                }

                is ResponseState.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }

                else -> Unit
            }
        }
    }

    private fun emit(event: NftNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}

class NftDetailViewModel(
    private val repository: NftRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NftDetailState())
    val state = _state.asStateFlow()

    private val _navEvents = Channel<NftNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    fun onEvent(event: NftDetailEvent) {
        when (event) {
            is NftDetailEvent.Load -> load(event.tokenId)
            NftDetailEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
            NftDetailEvent.BackClicked -> viewModelScope.launch {
                _navEvents.send(NftNavEvent.Back)
            }
        }
    }

    private fun load(tokenId: Long) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        when (val result = repository.getToken(tokenId)) {
            is ResponseState.Success -> _state.update {
                it.copy(
                    isLoading = false,
                    item = result.data.toItem(),
                    owner = result.data.owner
                )
            }

            is ResponseState.Error -> _state.update {
                it.copy(isLoading = false, error = result.message)
            }

            else -> Unit
        }
    }
}
