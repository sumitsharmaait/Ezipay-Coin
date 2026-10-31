package com.app.ezipaycoin.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {
    var token: String? = ""

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
