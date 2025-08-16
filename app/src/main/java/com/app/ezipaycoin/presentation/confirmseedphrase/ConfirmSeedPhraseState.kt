package com.app.ezipaycoin.presentation.confirmseedphrase

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.utils.ResponseState

data class ConfirmSeedPhraseState(
    val isAllConfirmed: Boolean = false,
    val currentConfirmationIndex : Int = 0,
    val confirmedIndex : Int = 0,
    val distractSeeds : List<String> = emptyList(),
    var confirmedSeeds : MutableList<String> = mutableListOf(),
    var responseState: ResponseState<BaseResponse<NonceResponse>> = ResponseState.Loading

)
