package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.CompleteTaskRequest
import com.app.ezipaycoin.data.remote.dto.response.CheckInResponse
import com.app.ezipaycoin.data.remote.dto.response.CheckInStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.CompleteTaskResponse
import com.app.ezipaycoin.data.remote.dto.response.DailyTasksResponse
import com.app.ezipaycoin.data.remote.dto.response.RewardSummaryResponse
import com.app.ezipaycoin.data.remote.dto.response.StakingPoolResponse
import com.app.ezipaycoin.domain.repository.EarnRepository
import com.app.ezipaycoin.utils.ResponseState

class EarnRepoImpl(
    private val api: ApiService,
    private val chainId: Int,
) : EarnRepository {

    override suspend fun summary(status: String): ResponseState<RewardSummaryResponse> =
        envelopeCall { api.getEarnSummary(status, chainId) }

    override suspend fun checkInStatus(month: String): ResponseState<CheckInStatusResponse> =
        envelopeCall { api.getCheckInStatus(month) }

    override suspend fun checkIn(date: String, idempotencyKey: String): ResponseState<CheckInResponse> =
        envelopeCall { api.checkIn(date, idempotencyKey) }

    override suspend fun tasks(date: String): ResponseState<DailyTasksResponse> =
        envelopeCall { api.getDailyTasks(date) }

    override suspend fun completeTask(
        taskId: Int,
        date: String,
        watchedSeconds: Int?,
        idempotencyKey: String
    ): ResponseState<CompleteTaskResponse> =
        envelopeCall { api.completeTask(taskId, CompleteTaskRequest(date, watchedSeconds), idempotencyKey) }

    override suspend fun stakingPools(): ResponseState<List<StakingPoolResponse>> =
        envelopeCall { api.getStakingPools(chainId) }
}
