package com.app.ezipaycoin.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    suspend fun setWalletCreated(created : Boolean)
    suspend fun isWalletCreate() : Flow<Boolean>
}