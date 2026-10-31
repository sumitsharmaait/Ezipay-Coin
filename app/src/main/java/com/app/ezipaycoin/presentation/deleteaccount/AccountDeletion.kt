package com.app.ezipaycoin.presentation.deleteaccount

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.utils.PasswordGate
import retrofit2.HttpException

sealed class DeletionResult {
    /** Deleted on the server and wiped from this device. */
    data object Deleted : DeletionResult()
    data class WrongPassword(val attemptsLeft: Int) : DeletionResult()
    data class LockedOut(val retryAfterMillis: Long) : DeletionResult()
    /** Nothing was changed on the server or on the device. */
    data class Failed(val message: String) : DeletionResult()
}

/**
 * Order matters: check the password, call the server, and wipe the device only after the server
 * confirmed. A failed call leaves the wallet on the device so the user can retry or back it up.
 */
class AccountDeletion(
    private val passwordMatches: suspend (entered: String) -> Boolean,
    private val gate: PasswordGate,
    private val deleteRemote: suspend (idempotencyKey: String) -> BaseResponse<String?>,
    private val wipeLocal: suspend () -> Unit,
    private val newKey: () -> String
) {
    /** One key per user action: a retry after a timeout reuses it. */
    private var pendingKey: String? = null

    suspend fun run(enteredPassword: String): DeletionResult {
        val matched = passwordMatches(enteredPassword)
        when (val check = gate.check(enteredPassword) { matched }) {
            is PasswordGate.Result.Wrong -> return DeletionResult.WrongPassword(check.attemptsLeft)
            is PasswordGate.Result.LockedOut -> return DeletionResult.LockedOut(check.retryAfterMillis)
            is PasswordGate.Result.Ok -> Unit
        }
        val key = pendingKey ?: newKey().also { pendingKey = it }
        val response = try {
            deleteRemote(key)
        } catch (e: HttpException) {
            return DeletionResult.Failed(
                if (e.code() == 401) "Your session has expired. Restart the app and try again."
                else "The account could not be deleted. Please try again."
            )
        } catch (e: Exception) {
            return DeletionResult.Failed("Could not reach the server. Check your connection and try again.")
        }
        if (!response.apiStatus) {
            pendingKey = null
            return DeletionResult.Failed(
                when (response.apiMessage) {
                    "FEATURE_DISABLED" -> "Account deletion is temporarily unavailable"
                    "CONFIRMATION_REQUIRED" -> "The deletion was not confirmed"
                    else -> "The account could not be deleted. Please try again."
                }
            )
        }
        return try {
            wipeLocal()
            DeletionResult.Deleted
        } catch (e: Exception) {
            DeletionResult.Failed("The account was deleted but this device could not be cleared. Reinstall the app.")
        }
    }
}
