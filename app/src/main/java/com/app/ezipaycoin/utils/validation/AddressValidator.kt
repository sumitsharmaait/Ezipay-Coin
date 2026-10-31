package com.app.ezipaycoin.utils.validation

/** Recipient address checks for the send screen. Returns an error message, or null when valid. */
object AddressValidator {
    private val hex40 = Regex("^0x[0-9a-fA-F]{40}$")
    private val hexChars = Regex("^[0-9a-fA-F]*$")
    const val FORMAT_ERROR = "Enter a valid address: 0x followed by 40 hex characters"
    const val CHECKSUM_ERROR = "Address checksum is wrong. Check the address for typos"
    const val OWN_ADDRESS_ERROR = "You cannot send to your own address"

    /** Full check, used on submit. */
    fun validate(input: String, ownAddress: String?): String? {
        val address = input.trim()
        if (!hex40.matches(address)) return FORMAT_ERROR
        if (!hasValidChecksum(address)) return CHECKSUM_ERROR
        if (ownAddress != null && address.equals(ownAddress.trim(), ignoreCase = true)) return OWN_ADDRESS_ERROR
        return null
    }

    /**
     * Check while the user types: an incomplete address is not an error yet, but a character
     * that can never be valid, or an overlong value, is flagged at once.
     */
    fun validateWhileTyping(input: String, ownAddress: String?): String? {
        val address = input.trim()
        if (address.isEmpty()) return null
        val body = if (address.startsWith("0x") || address.startsWith("0X")) address.drop(2) else return FORMAT_ERROR
        if (!hexChars.matches(body) || body.length > 40) return FORMAT_ERROR
        if (body.length < 40) return null
        return validate(address, ownAddress)
    }

    /** All-lower or all-upper addresses carry no checksum; mixed case must match EIP-55. */
    fun hasValidChecksum(address: String): Boolean {
        val body = address.removePrefix("0x").removePrefix("0X")
        if (body == body.lowercase() || body == body.uppercase()) return true
        return toChecksum(body) == body
    }

    fun toChecksum(hexBody: String): String {
        val lower = hexBody.lowercase()
        val hash = Keccak256.hash(lower.toByteArray(Charsets.US_ASCII))
        return buildString {
            lower.forEachIndexed { i, c ->
                val nibble = (hash[i / 2].toInt() shr (if (i % 2 == 0) 4 else 0)) and 0xF
                append(if (c in 'a'..'f' && nibble >= 8) c.uppercaseChar() else c)
            }
        }
    }
}
