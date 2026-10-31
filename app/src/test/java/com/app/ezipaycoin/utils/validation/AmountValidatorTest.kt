package com.app.ezipaycoin.utils.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class AmountValidatorTest {
    private fun d(s: String) = BigDecimal(s)

    @Test
    fun parsesPlainDecimals() {
        assertEquals(d("1.5"), AmountValidator.parse("1.5"))
        assertEquals(d("0.5"), AmountValidator.parse(".5"))
        assertNotNull(AmountValidator.parse(" 10 "))
    }

    @Test
    fun rejectsMalformedZeroAndNegative() {
        listOf("", " ", "abc", "1e5", "-1", "0", "0.0", "1.2.3", "1,5", "--1", "+1").forEach {
            assertNull(it, AmountValidator.parse(it))
        }
    }

    @Test
    fun decimalLimitIsEighteen() {
        assertNull(AmountValidator.validateInput("0.123456789012345678"))
        assertEquals(AmountValidator.TOO_MANY_DECIMALS, AmountValidator.validateInput("0.1234567890123456789"))
        assertNull(AmountValidator.validateInput("1.1000000000000000000000")) // trailing zeros only
    }

    @Test
    fun blankAndInvalidMessages() {
        assertEquals("Please enter an amount", AmountValidator.validateInput(""))
        assertEquals(AmountValidator.INVALID, AmountValidator.validateInput("x"))
    }

    @Test
    fun tokenAmountCannotExceedBalance() {
        assertNull(AmountValidator.validateFunds(d("5"), "EZPT", d("5"), d("1"), d("0.001")))
        assertEquals(AmountValidator.INSUFFICIENT_BALANCE, AmountValidator.validateFunds(d("5.1"), "EZPT", d("5"), d("1"), d("0.001")))
    }

    @Test
    fun tokenNeedsBnbForFee() {
        assertEquals(AmountValidator.NO_FEE_BNB, AmountValidator.validateFunds(d("1"), "USDT", d("5"), d("0.0001"), d("0.001")))
        assertNull(AmountValidator.validateFunds(d("1"), "USDT", d("5"), d("0.01"), d("0.001")))
    }

    @Test
    fun bnbAmountPlusFeeMustFit() {
        assertNull(AmountValidator.validateFunds(d("0.999"), "BNB", d("1"), d("1"), d("0.001")))
        assertNotNull(AmountValidator.validateFunds(d("0.9995"), "BNB", d("1"), d("1"), d("0.001")))
    }

    @Test
    fun feeSkippedWhenUnknown() {
        assertNull(AmountValidator.validateFunds(d("1"), "BNB", d("1"), d("1"), null))
    }

    @Test
    fun weiConversionRoundTrips() {
        assertEquals(java.math.BigInteger("1500000000000000000"), AmountValidator.toWei(d("1.5")))
        assertEquals(0, d("0.0021").compareTo(AmountValidator.weiToBnb(java.math.BigInteger("2100000000000000"))))
    }
}
