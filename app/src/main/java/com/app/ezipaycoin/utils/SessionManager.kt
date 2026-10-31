package com.app.ezipaycoin.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {
    var token: String? = ""
        set(value) {
            field = value
            if (!value.isNullOrEmpty()) expiredSignalled = false
        }

    @Volatile
    private var expiredSignalled = false

    private val _sessionExpired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Emits once when the server rejected the session and signing in again did not work. */
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        if (expiredSignalled) return
        expiredSignalled = true
        _sessionExpired.tryEmit(Unit)
    }

    private val _transferEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Emits when a push message arrives while the app is open, so balances can be refreshed. */
    val transferEvents: SharedFlow<Unit> = _transferEvents.asSharedFlow()

    fun notifyTransferActivity() {
        _transferEvents.tryEmit(Unit)
    }
}
