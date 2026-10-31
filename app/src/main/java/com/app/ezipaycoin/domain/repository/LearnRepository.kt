package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse
import com.app.ezipaycoin.utils.ResponseState

interface LearnRepository {
    suspend fun items(): ResponseState<List<LearnItemResponse>>
}
