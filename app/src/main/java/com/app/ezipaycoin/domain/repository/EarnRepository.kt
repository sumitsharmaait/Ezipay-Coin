package com.app.ezipaycoin.domain.repository

import com.app.ezipaycoin.data.remote.dto.response.CheckInResponse
import com.app.ezipaycoin.data.remote.dto.response.CheckInStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.CompleteTaskResponse
import com.app.ezipaycoin.data.remote.dto.response.DailyTasksResponse
import com.app.ezipaycoin.data.remote.dto.response.RewardSummaryResponse
import com.app.ezipaycoin.data.remote.dto.response.StakingPoolResponse
import com.app.ezipaycoin.utils.ResponseState

interface EarnRepository {
    suspend fun summary(status: String): ResponseState<RewardSummaryResponse>
    suspend fun checkInStatus(month: String): ResponseState<CheckInStatusResponse>
    /** Performs the check-in for [date] (`yyyy-MM-dd`). [idempotencyKey] makes a retry safe. */
    suspend fun checkIn(date: String, idempotencyKey: String): ResponseState<CheckInResponse>
    suspend fun tasks(date: String): ResponseState<DailyTasksResponse>
    suspend fun completeTask(
        taskId: Int,
        date: String,
        watchedSeconds: Int?,
        idempotencyKey: String
    ): ResponseState<CompleteTaskResponse>
    suspend fun stakingPools(): ResponseState<List<StakingPoolResponse>>
}
