package com.margiq.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class IndianCurrencyFormatterTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("₹48.00", IndianCurrencyFormatter.formatCurrency(BigDecimal("48.00")))
        assertEquals("₹135.00", IndianCurrencyFormatter.formatCurrency(BigDecimal("135.00")))
        assertEquals("₹1,234.56", IndianCurrencyFormatter.formatCurrency(BigDecimal("1234.56")))
        assertEquals("₹1,23,456.78", IndianCurrencyFormatter.formatCurrency(BigDecimal("123456.78")))
        assertEquals("₹1,23,45,678.90", IndianCurrencyFormatter.formatCurrency(BigDecimal("12345678.90")))
    }

    @Test
    fun testNegativeCurrencyFormatting() {
        assertEquals("-₹25.00", IndianCurrencyFormatter.formatCurrency(BigDecimal("-25.00")))
        assertEquals("-₹1,500.00", IndianCurrencyFormatter.formatCurrency(BigDecimal("-1500.00")))
    }

    @Test
    fun testPercentageFormatting() {
        assertEquals("62.67%", IndianCurrencyFormatter.formatPercentage(BigDecimal("62.66666")))
        assertEquals("167.86%", IndianCurrencyFormatter.formatPercentage(BigDecimal("167.85714")))
        assertEquals("0.00%", IndianCurrencyFormatter.formatPercentage(BigDecimal.ZERO))
        assertEquals("N/A", IndianCurrencyFormatter.formatPercentage(null))
    }

    @Test
    fun testInputParsing() {
        assertEquals(0, BigDecimal("48.00").compareTo(IndianCurrencyFormatter.parseInput("48.00")))
        assertEquals(0, BigDecimal("135.00").compareTo(IndianCurrencyFormatter.parseInput("₹135.00")))
        assertEquals(0, BigDecimal("1234.50").compareTo(IndianCurrencyFormatter.parseInput("1,234.50")))
        assertEquals(0, BigDecimal("5.00").compareTo(IndianCurrencyFormatter.parseInput("5%")))
        assertNull(IndianCurrencyFormatter.parseInput(""))
        assertNull(IndianCurrencyFormatter.parseInput("abc"))
    }
}
