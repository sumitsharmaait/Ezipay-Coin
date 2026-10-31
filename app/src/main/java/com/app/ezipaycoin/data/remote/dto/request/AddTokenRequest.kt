package com.app.ezipaycoin.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * POST Contract/AddToken — registers a custom token against the user's wallet.
 * Sent as JSON body, wrapped server-side; response comes back as BaseResponse<TokenMetadataResponse>.
 *
 * For "auto-fetch with manual fallback": the UI first calls validate (GET Contract/ValidateToken)
 * which fills symbol/name/decimals/logo. If that fails, the user types them in and they are sent here.
 */
@Serializable
data class AddTokenRequest(
    val ContractAddress: String,
    val ChainId: Int,
    val Symbol: String,
    val TokenName: String,
    val Decimals: Int,
    val TokenLogo: String? = null
)
