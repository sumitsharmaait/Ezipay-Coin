package com.app.ezipaycoin.presentation.importfromseed

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.NonceResponse
import com.app.ezipaycoin.utils.ResponseState

data class ImportFromSeedState(
    var seedPhrase: String = "",
    var newPassword: String = "",
    var newPasswordVisible: Boolean = false,
    var confirmPassword: String = "",
    var confirmPasswordVisible: Boolean = false,
    var signInWithFaceId: Boolean = false,
    var seedPhraseError: String = "",
    var newPasswordError: String = "",
    var confirmPasswordError: String = "",
    var responseState: ResponseState<BaseResponse<NonceResponse>> = ResponseState.Idle
)
