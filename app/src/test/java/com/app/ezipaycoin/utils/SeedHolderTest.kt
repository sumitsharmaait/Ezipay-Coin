package com.app.ezipaycoin.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedHolderTest {

    @After
    fun tearDown() = SeedHolder.clear()

    @Test
    fun holdsAndClears() {
        SeedHolder.set(listOf("a", "b"))
        assertEquals(listOf("a", "b"), SeedHolder.peek())
        SeedHolder.clear()
        assertTrue(SeedHolder.peek().isEmpty())
    }

    @Test
    fun keepsACopy() {
        val src = mutableListOf("a", "b")
        SeedHolder.set(src)
        src.clear()
        assertEquals(listOf("a", "b"), SeedHolder.peek())
    }

    @Test
    fun seedPhraseRouteCarriesNoArguments() {
        // A data object has no fields, so nothing sensitive can end up in saved navigation state.
        val fields = com.app.ezipaycoin.navigation.Screen.Auth.SeedPhraseVerify::class.java.declaredFields
            .filterNot { it.isSynthetic || java.lang.reflect.Modifier.isStatic(it.modifiers) }
        assertTrue(fields.isEmpty())
    }
}
