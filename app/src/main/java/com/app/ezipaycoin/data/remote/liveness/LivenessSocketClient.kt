package com.app.ezipaycoin.data.remote.liveness

import com.app.ezipaycoin.data.remote.dto.LivenessClientMessage
import com.app.ezipaycoin.data.remote.dto.LivenessServerMessage
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*

sealed interface LivenessSocketEvent {
    data class Message(val msg: LivenessServerMessage) : LivenessSocketEvent
    data class Failure(val error: String) : LivenessSocketEvent
    data object Closed : LivenessSocketEvent
}

class LivenessSocketClient(
    private val client: OkHttpClient,
    private val gson: Gson = Gson()
) {
    private var socket: WebSocket? = null

    fun connect(wsUrl: String, token: String): Flow<LivenessSocketEvent> = callbackFlow {
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(ws: WebSocket, text: String) {
                runCatching { gson.fromJson(text, LivenessServerMessage::class.java) }
                    .onSuccess { trySend(LivenessSocketEvent.Message(it)) }
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                trySend(LivenessSocketEvent.Failure(t.message ?: "Socket error"))
                close()
            }
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                trySend(LivenessSocketEvent.Closed); close()
            }
        }
        socket = client.newWebSocket(request, listener)
        awaitClose { socket?.close(1000, "client closed") }
    }

    fun send(msg: LivenessClientMessage) {
        socket?.send(Gson().toJson(msg))
    }

    fun abort() {
        socket?.send(Gson().toJson(LivenessClientMessage(type = "ABORT")))
        socket?.close(1000, "aborted")
    }
}
