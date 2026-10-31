package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs an API call and unwraps the `{ apiStatus, apiMessage, error, apiData }` envelope.
 *
 * A refused request (`apiStatus == false`) keeps the server's code in [ResponseState.Error.message]
 * (for example `ALREADY_CHECKED_IN`) so callers can map it to text.
 */
internal suspend fun <T> envelopeCall(call: suspend () -> BaseResponse<T>): ResponseState<T> =
    withContext(Dispatchers.IO) {
        try {
            val body = call()
            val data = body.apiData
            when {
                !body.apiStatus -> ResponseState.Error(body.apiMessage ?: body.error ?: "Request failed.")
                data == null -> ResponseState.Error("The server returned no data.")
                else -> ResponseState.Success(data)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            ResponseState.Error(
                when (e.code()) {
                    401 -> "Your session has expired. Please unlock your wallet again."
                    404 -> "NOT_FOUND"
                    else -> "Request failed (${e.code()})."
                }
            )
        } catch (e: IOException) {
            ResponseState.Error("Can't reach the network. Check your connection and try again.")
        } catch (e: Exception) {
            ResponseState.Error(e.message ?: "Something went wrong.")
        }
    }
