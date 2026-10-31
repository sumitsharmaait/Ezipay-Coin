package com.app.ezipaycoin.presentation.kyc

sealed class KycEvent {

    // --- 1. Personal details ---
    data class FullNameChanged(val value: String) : KycEvent()
    data class DateOfBirthChanged(val value: String) : KycEvent()
    data class NationalityChanged(val value: String) : KycEvent()

    // --- 2. Residential address ---
    data class AddressLineChanged(val value: String) : KycEvent()
    data class CityChanged(val value: String) : KycEvent()
    data class PostalCodeChanged(val value: String) : KycEvent()
    data class CountryChanged(val value: String) : KycEvent()
    data class ProofOfAddressPicked(val path: String) : KycEvent()

    // --- 3. Identity document ---
    // id matches DocSpec.id in KycDocumentCatalog (e.g. "PASSPORT", "AADHAAR")
    data class DocumentTypeSelected(val id: String) : KycEvent()
    data class DocumentNumberChanged(val value: String) : KycEvent()
    data class FrontImagePicked(val path: String) : KycEvent()
    data class BackImagePicked(val path: String) : KycEvent()
    data class SelfieImagePicked(val path: String) : KycEvent()

    // --- 4. Video / liveness ---
    data class LivenessToggled(val enabled: Boolean) : KycEvent()
    data object StartLiveness : KycEvent()
    data class VideoRecorded(val path: String) : KycEvent()

    data object StopLiveness : KycEvent()
    data class LivenessFrame(val base64: String) : KycEvent()

    // --- 5. Consent ---
    data class ConsentToggled(val accepted: Boolean) : KycEvent()

    // --- submission / lifecycle ---
    data object Submit : KycEvent()
    data class DismissDialog(val which: Int) : KycEvent()
    data object Retry : KycEvent()
}
