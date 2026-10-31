package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Returned by GET Contract/ValidateToken and POST Contract/AddToken, wrapped in [BaseResponse].
 * On validate, the chain-derived metadata is returned so the form can prefill.
 */
@Serializable
data class TokenMetadataResponse(
    val contractAddress: String = "",
    val symbol: String = "",
    val tokenName: String = "",
    val decimals: Int = 18,
    val tokenLogo: String? = null,
    val chainId: Int = 0,
    val isValid: Boolean = false
)
