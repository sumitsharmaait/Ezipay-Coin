package com.app.ezipaycoin.presentation.dashboard.earn

import com.app.ezipaycoin.data.remote.dto.response.CheckInStatusResponse
import com.app.ezipaycoin.data.remote.dto.response.DailyTask
import com.app.ezipaycoin.data.remote.dto.response.StakingPoolResponse

/** Result of loading one block of the Earn screen. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val data: T) : Load<T>
    data class Failed(val message: String) : Load<Nothing>
}

data class RewardTotals(
    val symbol: String,
    val claimable: Double,
    val locked: Double,
) {
    val total: Double get() = claimable + locked
}

data class EarnState(
    val totals: Load<RewardTotals> = Load.Loading,
    val checkIn: Load<CheckInStatusResponse> = Load.Loading,
    val tasks: Load<List<DailyTask>> = Load.Loading,
    val pools: Load<List<StakingPoolResponse>> = Load.Loading,
    val checkInBusy: Boolean = false,
    val completingTaskId: Int? = null,
    /** One-line result of the last user action (reward earned, or why it was refused). */
    val message: String? = null,
)
