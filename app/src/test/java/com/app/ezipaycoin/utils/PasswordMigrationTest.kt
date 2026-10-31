package com.app.ezipaycoin.utils

import com.app.ezipaycoin.data.remote.dto.UserPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordMigrationTest {

    private class FakeStore(var value: UserPreferences, var failOnUpdate: Int = -1) : PreferencesAccess {
        var updates = 0
        override suspend fun read() = value
        override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
            updates++
            if (updates == failOnUpdate) throw java.io.IOException("simulated crash before write $updates")
            value = transform(value)
        }
    }

    private val iter = 1_000
    private fun legacy() = UserPreferences(isWalletCreated = true, password = "pass-1234", walletAddress = "0xabc", seedPhrase = "seed")

    @Test
    fun migratesPlaintextToVerifierAndKeepsEverythingElse() = runTest {
        val store = FakeStore(legacy())
        assertEquals(MigrationOutcome.Migrated, PasswordMigration(store, iter).run())
        assertNull(store.value.password)
        assertTrue(PasswordCredentials.matches(store.value, "pass-1234"))
        assertFalse(PasswordCredentials.matches(store.value, "wrong"))
        assertEquals("0xabc", store.value.walletAddress)
        assertEquals("seed", store.value.seedPhrase)
    }

    @Test
    fun secondRunDoesNothing() = runTest {
        val store = FakeStore(legacy())
        PasswordMigration(store, iter).run()
        val before = store.value
        val updates = store.updates
        assertEquals(MigrationOutcome.NothingToDo, PasswordMigration(store, iter).run())
        assertEquals(before, store.value)
        assertEquals(updates, store.updates)
    }

    @Test
    fun freshInstallWithoutPasswordIsUntouched() = runTest {
        val store = FakeStore(UserPreferences())
        assertEquals(MigrationOutcome.NothingToDo, PasswordMigration(store, iter).run())
        assertEquals(0, store.updates)
    }

    @Test
    fun crashBeforeVerifierWriteLeavesPlaintextUsable() = runTest {
        val store = FakeStore(legacy(), failOnUpdate = 1)
        try { PasswordMigration(store, iter).run() } catch (e: java.io.IOException) { }
        assertEquals("pass-1234", store.value.password)
        assertTrue(PasswordCredentials.matches(store.value, "pass-1234"))
        // next start finishes the job
        assertEquals(MigrationOutcome.Migrated, PasswordMigration(store, iter).run())
        assertNull(store.value.password)
    }

    @Test
    fun crashBetweenVerifierAndPlaintextDeletionKeepsBothAndRecovers() = runTest {
        val store = FakeStore(legacy(), failOnUpdate = 2)
        try { PasswordMigration(store, iter).run() } catch (e: java.io.IOException) { }
        // verifier is durable, plaintext still there: the password still works
        assertEquals("pass-1234", store.value.password)
        assertTrue(store.value.passwordVerifier != null)
        assertTrue(PasswordCredentials.matches(store.value, "pass-1234"))
        val verifierBefore = store.value.passwordVerifier

        assertEquals(MigrationOutcome.Migrated, PasswordMigration(store, iter).run())
        assertNull(store.value.password)
        assertEquals(verifierBefore, store.value.passwordVerifier) // reused, not recomputed
        assertTrue(PasswordCredentials.matches(store.value, "pass-1234"))
    }

    @Test
    fun plaintextIsNeverClearedWhenVerifierCannotBeConfirmed() = runTest {
        // A store that silently drops the verifier write.
        val store = object : PreferencesAccess {
            var value = legacy()
            override suspend fun read() = value
            override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
                value = transform(value).copy(passwordVerifier = null)
            }
        }
        assertEquals(MigrationOutcome.Retained, PasswordMigration(store, iter).run())
        assertEquals("pass-1234", store.value.password)
    }

    @Test
    fun staleVerifierForADifferentPasswordIsReplaced() = runTest {
        val stale = PasswordVerifier.create("something-else", iter)
        val store = FakeStore(legacy().copy(passwordVerifier = stale))
        assertEquals(MigrationOutcome.Migrated, PasswordMigration(store, iter).run())
        assertTrue(PasswordCredentials.matches(store.value, "pass-1234"))
        assertFalse(PasswordCredentials.matches(store.value, "something-else"))
    }
}
