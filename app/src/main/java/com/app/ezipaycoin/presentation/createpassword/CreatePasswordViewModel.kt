package com.app.ezipaycoin.presentation.createpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreatePasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePasswordState())
    val uiState: StateFlow<CreatePasswordState> = _uiState.asStateFlow()
    val dataStore = UserPreferencesRepository.userPreferencesFlow
    private val _eventFlow = MutableSharedFlow<CreatePasswordVMEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val eventFlow = _eventFlow.asSharedFlow()


    fun onEvent(event: CreatePasswordEvent) {
        when (event) {
            is CreatePasswordEvent.IUnderstandChange -> {
                _uiState.update {
                    it.copy(understandState = event.iUnderstandChange)
                }
            }

            is CreatePasswordEvent.ConfirmPasswordChange -> {
                _uiState.update {
                    it.copy(confirmPassword = event.confirmPassword)
                }
            }

            is CreatePasswordEvent.ConfirmPasswordToggle -> {
                _uiState.update {
                    it.copy(confirmPasswordVisible = event.confirmPasswordToggle)
                }
            }

            is CreatePasswordEvent.NewPasswordChange -> {
                val passwordStrength = when {
                    event.newPassword.isEmpty() -> "" // No text when empty
                    event.newPassword.length < 8 -> "Weak"
                    event.newPassword.any { it.isDigit() } && event.newPassword.any { it.isLetter() } -> "Good"
                    else -> "Medium"
                }
                _uiState.update {
                    it.copy(newPassword = event.newPassword, passwordStrength = passwordStrength)
                }
            }

            is CreatePasswordEvent.NewPasswordToggle -> {
                _uiState.update {
                    it.copy(newPasswordVisible = event.passwordToggle)
                }
            }

            is CreatePasswordEvent.SignInWithFaceIdChange -> {
                _uiState.update {
                    updateBiometricEnabled(event.isFaceIdChange)
                    it.copy(signInWithFaceId = event.isFaceIdChange, bioMetricMessage = if (event.isFaceIdChange) "Biometric login enabled!" else "Authentication failed or cancelled")
                }
            }

            CreatePasswordEvent.SecureBtnClick -> {
                if (validateForm()) {
                    updatePassword()
                }
            }
        }

    }

    private fun updateBiometricEnabled(faceIdChange: Boolean) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    bioMetricAuthEnabled = faceIdChange
                )
            }
        }
    }

    private fun updatePassword() {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    password = _uiState.value.newPassword,
                )
            }
            val data = dataStore.data.first()
            if (data.isWalletCreated) {
                _eventFlow.emit(CreatePasswordVMEvent.MoveToSuccess)
            } else {
                _eventFlow.emit(CreatePasswordVMEvent.MoveToNext)
            }
        }

    }


    private fun validateForm(): Boolean {

        _uiState.update {
            it.copy(
                understandError = "",
                newPasswordError = "",
                confirmPasswordError = ""
            )
        }

        if (_uiState.value.newPassword.isBlank() || _uiState.value.newPassword.length < 8) {
            _uiState.update {
                it.copy(newPasswordError = "Must be at least 8 characters")
            }
            return false
        }

        if (_uiState.value.confirmPassword.isBlank() || _uiState.value.confirmPassword.length < 8) {
            _uiState.update {
                it.copy(confirmPasswordError = "Must be at least 8 characters")
            }
            return false
        }

        if (!_uiState.value.newPassword.equals(_uiState.value.confirmPassword, false)) {
            _uiState.update {
                it.copy(
                    confirmPasswordError = "Passwords are not same",
                    newPasswordError = "Passwords are not same"
                )
            }
            return false
        }

        if (!_uiState.value.understandState) {
            _uiState.update {
                it.copy(understandError = "Please Confirm")
            }
            return false
        }

        return true
    }

}