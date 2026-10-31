package com.app.ezipaycoin.presentation.kyc

import com.app.ezipaycoin.data.remote.dto.response.KycStatusResponse
import com.app.ezipaycoin.utils.ResponseState

data class KycState(
    // status fetch (drives the status screen)
    val statusResponse: ResponseState<KycStatusResponse> = ResponseState.Idle,

    // --- 1. Personal details ---
    val fullName: String = "",
    val dateOfBirth: String = "",      // formatted "DD / MM / YYYY"
    val nationality: String = "",

    // --- 2. Residential address ---
    val addressLine: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = "",
    val proofOfAddressPath: String = "",   // optional

    // --- 3. Identity document ---
    // matches DocSpec.id in KycDocumentCatalog (e.g. "PASSPORT", "AADHAAR")
    val selectedDocTypeId: String? = null,
    val documentNumber: String = "",
    val frontImagePath: String = "",
    val backImagePath: String = "",
    val selfieImagePath: String = "",

    // --- 4. Video / liveness ---
    val livenessEnabled: Boolean = true,   // toggle: SDK liveness vs recorded video
    val livenessVerified: Boolean = false, // set true when the SDK session passes
    val videoPath: String = "",            // recorded video temp path (when liveness off)

    val videoUploading: Boolean = false,
    val videoUploadError: String? = null,
    val liveness: LivenessUiState = LivenessUiState(),

    // --- 5. Consent ---
    val consentAccepted: Boolean = false,

    // submission result
    val submitResponse: ResponseState<KycStatusResponse> = ResponseState.Idle
) {
    /** Resolved document spec for the current selection (defaults to passport). */
    private val docSpec: DocSpec
        get() = KycDocumentCatalog.byId(selectedDocTypeId)

    /** Back image only required when the chosen document type is double-sided. */
    val isBackRequired: Boolean
        get() = docSpec.requiresBack

    // --- per-step completion (mirrors the screen's progress tracker) ---

    val isPersonalComplete: Boolean
        get() = fullName.isNotBlank() &&
                dateOfBirth.isNotBlank() &&
                nationality.isNotBlank()

    val isAddressComplete: Boolean
        get() = addressLine.isNotBlank() &&
                city.isNotBlank() &&
                postalCode.isNotBlank() &&
                country.isNotBlank()

    val isDocumentComplete: Boolean
        get() = selectedDocTypeId != null &&
                docSpec.isComplete(documentNumber) &&
                frontImagePath.isNotBlank() &&
                selfieImagePath.isNotBlank() &&
                (!isBackRequired || backImagePath.isNotBlank())

    /** Either an automated liveness pass or a recorded video satisfies this step. */
    val isLivenessComplete: Boolean
        get() = if (livenessEnabled) livenessVerified else videoPath.isNotBlank()

    val isFormValid: Boolean
        get() = isPersonalComplete &&
                isAddressComplete &&
                isDocumentComplete &&
                isLivenessComplete &&
                consentAccepted
}

data class LivenessUiState(
    val connecting: Boolean = false,
    val activeChallenge: String? = null,
    val step: Int = 0,
    val total: Int = 0,
    val error: String? = null
)
