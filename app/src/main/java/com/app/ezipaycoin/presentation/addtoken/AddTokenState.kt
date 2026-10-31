package com.app.ezipaycoin.presentation.addtoken

import com.app.ezipaycoin.data.remote.dto.response.TokenMetadataResponse
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.WalletManager

data class AddTokenState(
    val contractAddress: String = "",
    val chainId: Int = DEFAULT_CHAIN_ID,

    // editable fields — prefilled by auto-fetch, overridable as manual fallback
    val symbol: String = "",
    val tokenName: String = "",
    val decimals: String = "18",

    // true once a validate call has prefilled the fields, so UI can show "auto-filled" hint
    val autoFilled: Boolean = false,

    val validateResponse: ResponseState<TokenMetadataResponse> = ResponseState.Idle,
    val addResponse: ResponseState<TokenMetadataResponse> = ResponseState.Idle
) {
    val isAddressValidFormat: Boolean
        get() = contractAddress.startsWith("0x") && contractAddress.length == 42

    val isFormValid: Boolean
        get() = isAddressValidFormat &&
                symbol.isNotBlank() &&
                tokenName.isNotBlank() &&
                decimals.toIntOrNull() != null

    companion object {
        /** The build type's chain: BSC mainnet, or the local chain in the sandbox build. */
        val DEFAULT_CHAIN_ID: Int = WalletManager.CHAIN_ID.toInt()
    }
}
