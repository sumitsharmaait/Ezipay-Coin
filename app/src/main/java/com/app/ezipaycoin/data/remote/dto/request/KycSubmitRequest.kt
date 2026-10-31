package com.app.ezipaycoin.data.remote.dto.request

/**
 * Carrier for KYC submission. The actual upload is multipart (see [com.app.ezipaycoin.data.remote.api.ApiService.submitKyc]),
 * so this holds the local file references + metadata the ViewModel collects from the UI before
 * building the [okhttp3.MultipartBody.Part] list.
 *
 * documentType maps to the backend's accepted set:
 *   PASSPORT | DRIVING_LICENSE | NATIONAL_ID | AADHAAR | PAN
 *
 * The applicant / address / liveness / consent fields are nullable so the request
 * stays valid even if the backend hasn't enabled a given field yet. Drop unused
 * fields when assembling the multipart body in ApiService.
 */
data class KycSubmitRequest(
    // --- applicant details ---
    val fullName: String? = null,
    val dateOfBirth: String? = null,      // "DD / MM / YYYY" as entered; normalize server-side
    val nationality: String? = null,

    // --- residential address ---
    val addressLine: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val proofOfAddressPath: String? = null,  // optional document

    // --- identity document ---
    val documentType: String,
    val documentNumber: String,
    val frontImagePath: String,
    val backImagePath: String?,           // null for single-sided docs (e.g. Passport, PAN)
    val selfieImagePath: String,

    // --- video / liveness ---
    val videoPath: String? = null,        // set when recorded-video mode is used
    val livenessVerified: Boolean = false,// true when automated liveness passed

    // --- consent ---
    val consentAccepted: Boolean = false
)

enum class KycDocumentType(val apiValue: String, val label: String, val requiresBack: Boolean) {
    PASSPORT("PASSPORT", "Passport", false),
    DRIVING_LICENSE("DRIVING_LICENSE", "Driver's License", true),
    NATIONAL_ID("NATIONAL_ID", "National ID", true),
    AADHAAR("AADHAAR", "Aadhaar", true),
    PAN("PAN", "PAN Card", false);

    companion object {
        fun fromApiValue(value: String?): KycDocumentType? =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
    }
}
