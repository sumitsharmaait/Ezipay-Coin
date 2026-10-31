package com.app.ezipaycoin.data.remote.dto.request

/** Body of `POST Earn/checkin/tasks/{taskId}/complete`. [date] is `yyyy-MM-dd` (UTC). */
data class CompleteTaskRequest(
    val date: String,
    val watchedSeconds: Int? = null,
)
