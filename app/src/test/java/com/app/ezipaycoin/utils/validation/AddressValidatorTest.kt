package com.app.ezipaycoin.utils.validation

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddressValidatorTest {
    private val eip55 = listOf(
        "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed",
        "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359",
        "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB",
        "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"
    )

    @Test
    fun keccakKnownVectors() {
        fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", hex(Keccak256.hash(ByteArray(0))))
        assertEquals("4e03657aea45a94fc7d47ba826c8d667c0d1e6e33a64a036ec44f58fa12d6c45", hex(Keccak256.hash("abc".toByteArray())))
        // longer than one 136-byte block
        assertEquals(32, Keccak256.hash(ByteArray(300) { 7 }).size)
        assertArrayEquals(Keccak256.hash(ByteArray(300) { 7 }), Keccak256.hash(ByteArray(300) { 7 }))
    }

    @Test
    fun validChecksummedAddressesPass() {
        eip55.forEach { assertNull(it, AddressValidator.validate(it, null)) }
    }

    @Test
    fun badChecksumRejected() {
        val bad = "0x5AAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
        assertEquals(AddressValidator.CHECKSUM_ERROR, AddressValidator.validate(bad, null))
    }

    @Test
    fun lowerAndUpperCaseHaveNoChecksum() {
        val lower = eip55[0].lowercase()
        assertNull(AddressValidator.validate(lower, null))
        assertNull(AddressValidator.validate("0x" + eip55[0].drop(2).uppercase(), null))
    }

    @Test
    fun malformedRejected() {
        listOf("", "abc", "0x123", "0x" + "g".repeat(40), eip55[0] + "0", eip55[0].drop(2)).forEach {
            assertEquals(it, AddressValidator.FORMAT_ERROR, AddressValidator.validate(it, null))
        }
    }

    @Test
    fun ownAddressRejectedCaseInsensitive() {
        assertEquals(AddressValidator.OWN_ADDRESS_ERROR, AddressValidator.validate(eip55[0], eip55[0].lowercase()))
    }

    @Test
    fun whileTypingOnlyFlagsImpossibleInput() {
        assertNull(AddressValidator.validateWhileTyping("", null))
        assertNull(AddressValidator.validateWhileTyping("0x5aA", null))
        assertEquals(AddressValidator.FORMAT_ERROR, AddressValidator.validateWhileTyping("0xZZ", null))
        assertEquals(AddressValidator.FORMAT_ERROR, AddressValidator.validateWhileTyping("hello", null))
        assertEquals(AddressValidator.CHECKSUM_ERROR, AddressValidator.validateWhileTyping("0x5AAeb6053F3E94C9b9A09f33669435E7Ef1BeAed", null))
    }
}
