package com.app.ezipaycoin.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Returned by GET Users/KycStatus, wrapped in [BaseResponse].
 *
 * status values mirror the backend KYC state machine:
 *   NOT_STARTED | PENDING | APPROVED | REJECTED
 */
@Serializable
data class KycStatusResponse(
    val kycId: String? = null,
    val status: String = KycStatus.NOT_STARTED,
    val documentType: String? = null,
    val rejectionReason: String? = null,
    val submittedAt: String? = null,
    val reviewedAt: String? = null
)

object KycStatus {
    const val NOT_STARTED = "NOT_STARTED"
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}
