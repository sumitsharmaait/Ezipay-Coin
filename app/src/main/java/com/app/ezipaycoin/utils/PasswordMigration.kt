package com.app.ezipaycoin.utils

import com.app.ezipaycoin.data.remote.dto.UserPreferences
import kotlinx.coroutines.flow.first

/** The two operations the migration needs; backed by the DataStore in the app. */
interface PreferencesAccess {
    suspend fun read(): UserPreferences
    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}

enum class MigrationOutcome {
    /** No plaintext password is stored. */
    NothingToDo,

    /** The verifier is stored and the plaintext is gone. */
    Migrated,

    /** A step could not be confirmed; the plaintext is kept and the next start retries. */
    Retained
}

/**
 * Moves a legacy plaintext password to a verifier. Order is what keeps the wallet reachable:
 *  1. write the verifier (plaintext still present)
 *  2. read it back and check that it accepts the stored password
 *  3. only then clear the plaintext, and confirm that by reading again
 * A crash or failure after any step leaves a state the same code can finish. Safe to call repeatedly.
 */
class PasswordMigration(private val prefs: PreferencesAccess, private val iterations: Int = PasswordVerifier.DEFAULT_ITERATIONS) {

    suspend fun run(): MigrationOutcome {
        val start = prefs.read()
        val legacy = start.password
        if (legacy.isNullOrEmpty()) return MigrationOutcome.NothingToDo

        if (!PasswordVerifier.verify(start.passwordVerifier, legacy)) {
            val verifier = PasswordVerifier.create(legacy, iterations)
            prefs.update { current ->
                if (current.password == legacy) current.copy(passwordVerifier = verifier) else current
            }
            val written = prefs.read()
            if (!PasswordVerifier.verify(written.passwordVerifier, legacy)) return MigrationOutcome.Retained
        }

        prefs.update { current ->
            val plain = current.password
            if (plain != null && PasswordVerifier.verify(current.passwordVerifier, plain)) current.copy(password = null)
            else current
        }
        val end = prefs.read()
        return if (end.password == null && !end.passwordVerifier.isNullOrEmpty()) MigrationOutcome.Migrated
        else MigrationOutcome.Retained
    }
}

class DataStorePreferencesAccess(private val store: androidx.datastore.core.DataStore<UserPreferences>) : PreferencesAccess {
    override suspend fun read(): UserPreferences = store.data.first()
    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        store.updateData(transform)
    }
}
