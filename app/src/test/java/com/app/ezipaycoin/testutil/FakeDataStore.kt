package com.app.ezipaycoin.testutil

import androidx.datastore.core.DataStore
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory preferences store. No encryption, no files. */
class FakeDataStore(initial: UserPreferences = UserPreferences()) : DataStore<UserPreferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<UserPreferences> = state

    val current: UserPreferences get() = state.value

    override suspend fun updateData(transform: suspend (t: UserPreferences) -> UserPreferences): UserPreferences {
        val next = transform(state.value)
        state.value = next
        return next
    }

    companion object {
        /** Makes the UserPreferencesRepository singleton return [store]. Pair with [uninstall]. */
        fun install(store: FakeDataStore) {
            mockkObject(UserPreferencesRepository)
            every { UserPreferencesRepository.userPreferencesFlow } returns store
        }

        fun uninstall() = unmockkObject(UserPreferencesRepository)
    }
}
