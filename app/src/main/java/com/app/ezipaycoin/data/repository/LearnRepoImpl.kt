package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.response.LearnItemResponse
import com.app.ezipaycoin.domain.repository.LearnRepository
import com.app.ezipaycoin.utils.ResponseState

class LearnRepoImpl(private val api: ApiService) : LearnRepository {
    override suspend fun items(): ResponseState<List<LearnItemResponse>> =
        envelopeCall { api.getLearn() }
}
