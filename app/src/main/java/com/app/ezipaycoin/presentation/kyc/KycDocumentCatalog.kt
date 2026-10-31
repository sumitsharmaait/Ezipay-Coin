package com.app.ezipaycoin.presentation.kyc

import com.app.ezipaycoin.data.remote.dto.request.KycDocumentType

/**
 * Presentation-layer catalog of the identity documents the backend accepts,
 * each with a display label, a placeholder showing the expected shape, the
 * right keyboard, and a live input formatter + light client-side validity check.
 *
 * The set here is intentionally kept in lock-step with [KycDocumentType] so the
 * UI never offers a document the backend will reject. [id] equals the enum's
 * apiValue, so mapping back for submission is a direct lookup.
 *
 * Formatting philosophy:
 *  - [format] runs on every keystroke (grouping, casing, separators).
 *  - [placeholder] shows the expected shape (X = a free character).
 *  - [isComplete] is a client-side sanity check only, NOT authoritative
 *    verification — the backend remains the source of truth.
 */
data class DocSpec(
    val id: String,                 // == KycDocumentType.apiValue
    val label: String,
    val region: String,
    val placeholder: String,
    val requiresBack: Boolean,
    val keyboard: DocKeyboard,
    val format: (String) -> String,
    val isComplete: (String) -> Boolean
)

enum class DocKeyboard { TEXT, NUMBER, ALPHANUMERIC }

private fun digitsOnly(s: String) = s.filter { it.isDigit() }
private fun alnumUpper(s: String) = s.filter { it.isLetterOrDigit() }.uppercase()
private fun groupDigits(raw: String, chunk: Int, max: Int): String =
    digitsOnly(raw).take(max).chunked(chunk).joinToString(" ")

object KycDocumentCatalog {

    private val Passport = DocSpec(
        id = KycDocumentType.PASSPORT.apiValue,
        label = KycDocumentType.PASSPORT.label,
        region = "Global",
        placeholder = "X1234567",
        requiresBack = KycDocumentType.PASSPORT.requiresBack,
        keyboard = DocKeyboard.ALPHANUMERIC,
        format = { alnumUpper(it).take(9) },
        isComplete = { alnumUpper(it).length in 6..9 }
    )

    private val DrivingLicense = DocSpec(
        id = KycDocumentType.DRIVING_LICENSE.apiValue,
        label = KycDocumentType.DRIVING_LICENSE.label,
        region = "Global",
        placeholder = "XXXXXXXXXXXXXXX",
        requiresBack = KycDocumentType.DRIVING_LICENSE.requiresBack,
        keyboard = DocKeyboard.ALPHANUMERIC,
        format = { alnumUpper(it).take(20) },
        isComplete = { alnumUpper(it).length in 6..20 }
    )

    private val NationalId = DocSpec(
        id = KycDocumentType.NATIONAL_ID.apiValue,
        label = KycDocumentType.NATIONAL_ID.label,
        region = "Global",
        placeholder = "XXXXXXXXXXXX",
        requiresBack = KycDocumentType.NATIONAL_ID.requiresBack,
        keyboard = DocKeyboard.ALPHANUMERIC,
        format = { alnumUpper(it).take(20) },
        isComplete = { alnumUpper(it).length in 5..20 }
    )

    private val Aadhaar = DocSpec(
        id = KycDocumentType.AADHAAR.apiValue,
        label = KycDocumentType.AADHAAR.label,
        region = "India",
        placeholder = "XXXX XXXX XXXX",
        requiresBack = KycDocumentType.AADHAAR.requiresBack,
        keyboard = DocKeyboard.NUMBER,
        format = { groupDigits(it, 4, 12) },
        isComplete = { digitsOnly(it).length == 12 }
    )

    private val Pan = DocSpec(
        id = KycDocumentType.PAN.apiValue,
        label = KycDocumentType.PAN.label,
        region = "India",
        placeholder = "ABCDE1234F",
        requiresBack = KycDocumentType.PAN.requiresBack,
        keyboard = DocKeyboard.ALPHANUMERIC,
        format = { alnumUpper(it).take(10) },
        // 5 letters, 4 digits, 1 letter
        isComplete = { Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(alnumUpper(it)) }
    )

    /** Ordered for display. Kept in lock-step with KycDocumentType. */
    val all: List<DocSpec> = listOf(
        Passport,
        NationalId,
        DrivingLicense,
        Aadhaar,
        Pan
    )

    val default: DocSpec = Passport

    fun byId(id: String?): DocSpec = all.firstOrNull { it.id == id } ?: default
}
