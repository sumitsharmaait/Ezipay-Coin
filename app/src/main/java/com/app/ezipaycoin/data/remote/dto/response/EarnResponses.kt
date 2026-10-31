package com.app.ezipaycoin.data.remote.dto.response

// JSON shapes of the Earn, Staking (read) and Learn routes. camelCase as emitted by the API.
// Amounts arrive as strings with four decimals ("0.0000") except the summary total, a number.

/** `GET Earn/summary?status=CLAIMABLE|LOCKED|CLAIMED` */
data class RewardSummaryResponse(
    val tokenSymbol: String? = null,
    val status: String? = null,
    val totalAmount: Double? = null,
    val itemsCount: Int? = null,
)

/** `GET Earn/checkin/status?month=yyyy-MM` */
data class CheckInStatusResponse(
    val month: String? = null,
    val today: String? = null,
    val checkedInDates: List<String>? = null,
    val canCheckIn: Boolean = false,
    val currentStreak: Int = 0,
    val nextReward: NextReward? = null,
)

data class NextReward(
    val amount: String? = null,
    val tokenSymbol: String? = null,
    val lockDays: Int = 0,
)

/** `GET Earn/checkin?date=yyyy-MM-dd` (records today's check-in) */
data class CheckInResponse(
    val checkedInDate: String? = null,
    val newStreak: Int = 0,
    val reward: CheckInReward? = null,
)

data class CheckInReward(
    val amount: String? = null,
    val tokenSymbol: String? = null,
    val status: String? = null,
    val lockedUntil: String? = null,
    val ledgerId: String? = null,
)

/** `GET Earn/checkin/tasks?date=yyyy-MM-dd` */
data class DailyTasksResponse(
    val date: String? = null,
    val tasks: List<DailyTask>? = null,
)

data class DailyTask(
    val taskId: Int = 0,
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val requiredSeconds: Int? = null,
    val contentUrl: String? = null,
    val reward: TaskReward? = null,
    val status: String? = null,
) {
    val isCompleted: Boolean get() = status.equals("COMPLETED", ignoreCase = true)
}

data class TaskReward(
    val amount: String? = null,
    val tokenSymbol: String? = null,
)

/** `POST Earn/checkin/tasks/{id}/complete` */
data class CompleteTaskResponse(
    val taskId: Int = 0,
    val date: String? = null,
    val taskStatus: String? = null,
    val reward: CompleteTaskReward? = null,
)

data class CompleteTaskReward(
    val amount: String? = null,
    val tokenSymbol: String? = null,
    val status: String? = null,
    val ledgerId: String? = null,
)

/** `GET earn/Staking/pools?chainId=` (read only) */
data class StakingPoolResponse(
    val poolId: Int = 0,
    val name: String? = null,
    val apr: Double? = null,
    val lockDays: Int? = null,
    val minStake: String? = null,
    val isActive: Boolean = false,
    val token: PoolToken? = null,
    val stakingContract: String? = null,
)

data class PoolToken(
    val symbol: String? = null,
    val address: String? = null,
)

/** `GET learn` */
data class LearnItemResponse(
    val id: Int? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val icon: String? = null,
    val type: String? = null,
    val resourceUrl: String? = null,
    val language: String? = null,
    val isFeatured: Boolean? = null,
    val sortOrder: Int? = null,
    val status: String? = null,
)
