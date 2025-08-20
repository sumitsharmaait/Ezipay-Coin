package com.app.ezipaycoin.data.repository

import androidx.datastore.core.DataStore
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserDataRepoImpl(private val dataStore: DataStore<UserPreferences>) : UserDataRepository {

    override suspend fun setWalletCreated(created: Boolean) {
        dataStore.updateData {
            it.copy(isWalletCreated = created)
        }
    }

    override suspend fun isWalletCreate(): Flow<Boolean> {
        return dataStore.data.map {
            it.isWalletCreated
        }.distinctUntilChanged()
    }
}