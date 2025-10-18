package com.app.ezipaycoin.presentation.profile

import android.net.Uri
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.utils.ResponseState

data class MyProfileState(
    var selectedImageUri : Uri? = null,
    val profile: String? = "",
    val walletAddress: String = "",
    val name: String = "Not Available",
    val email: String = "Not Available",
    var responseState: ResponseState<BaseResponse<String>> = ResponseState.Idle,
    var logoutResponseState: ResponseState<BaseResponse<String>> = ResponseState.Idle,
    var uploadImageResponseState: ResponseState<BaseResponse<String>> = ResponseState.Idle
)
