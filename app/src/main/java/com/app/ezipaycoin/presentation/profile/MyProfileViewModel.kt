package com.app.ezipaycoin.presentation.profile

import androidx.datastore.core.DataStore
import com.app.ezipaycoin.utils.UserMessages
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ezipaycoin.data.remote.dto.UserPreferences
import com.app.ezipaycoin.data.remote.dto.UserPreferencesRepository
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import com.app.ezipaycoin.domain.repository.AuthRepository
import com.app.ezipaycoin.domain.repository.UserProfileRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MyProfileViewModel(
    private val repository: AuthRepository,
    private val profileRepository: UserProfileRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyProfileState())
    val uiState: StateFlow<MyProfileState> = _uiState.asStateFlow()
    var prefs: DataStore<UserPreferences>? = null

    init {
        viewModelScope.launch {
            prefs = UserPreferencesRepository.userPreferencesFlow
            var data = prefs?.data?.first()
            _uiState.update { state ->
                state.copy(
                    walletAddress = data?.walletAddress ?: state.walletAddress,
                    email = data?.userEmail ?: state.email,
                    profile = data?.userProfile ?: state.profile,
                    name = data?.userName ?: state.name
                )
            }
            loadProfile()
        }
    }

    /** Loads the profile from the server and caches it; the cached values stay on screen if that fails. */
    private suspend fun loadProfile() {
        val repo = profileRepository ?: return
        try {
            val res = repo.getMe()
            if (!res.apiStatus) return
            val cached = prefs?.data?.first()
            val merged = ProfileMerge.merge(
                ProfileFields(cached?.userName, cached?.userEmail, cached?.userProfile),
                res.apiData
            )
            prefs?.updateData {
                it.copy(userName = merged.name, userEmail = merged.email, userProfile = merged.profile)
            }
            _uiState.update {
                it.copy(
                    name = merged.name ?: it.name,
                    email = merged.email ?: it.email,
                    profile = merged.profile ?: it.profile
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline or server error: keep showing the cached profile.
        }
    }

    fun onEvent(event: MyProfileEvent) {
        when (event) {
            is MyProfileEvent.UpdateName -> {
                _uiState.update {
                    it.copy(name = event.name)
                }
                updateName()
            }

            is MyProfileEvent.LogOut -> {
                onLogout()
            }

            is MyProfileEvent.ImageSelected -> {
                _uiState.update {
                    it.copy(selectedImageUri = event.imageUri)
                }
                uploadImage(event.imageFile)
            }
        }
    }


    private fun uploadImage(imageFile: File) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    uploadImageResponseState = ResponseState.Loading,
                )
            }
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart =
                    MultipartBody.Part.createFormData("File", imageFile.name, requestFile)
                val response = repository.updateImage(imagePart)
                if (response.apiStatus) {
                    prefs?.updateData {
                        it.copy(userProfile = response.apiData)
                    }
                    _uiState.update {
                        it.copy(
                            uploadImageResponseState = ResponseState.Success(response)
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        uploadImageResponseState = ResponseState.Error(
                            UserMessages.of(e, "Unknown error")
                        )
                    )
                }
            }
        }

    }

    private fun onLogout() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    logoutResponseState = ResponseState.Loading,
                )
            }
            try {
                val logout = repository.logout()
                if (logout.apiStatus) {
                    _uiState.update {
                        it.copy(
                            logoutResponseState = ResponseState.Success(logout)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        logoutResponseState = ResponseState.Error(
                            UserMessages.of(e, "Unknown error")
                        )
                    )
                }
            }


        }
    }


    private fun updateName() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    responseState = ResponseState.Loading,
                )
            }

            try {
                val updateProfileRes =
                    repository.updateProfile(
                        UpdateProfileRequest(
                            _uiState.value.name,
                            _uiState.value.email
                        )
                    )
                if (updateProfileRes.apiStatus) {
                    prefs?.updateData {
                        it.copy(userName = _uiState.value.name)
                    }
                    _uiState.update {
                        it.copy(
                            responseState = ResponseState.Success(updateProfileRes)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        responseState = ResponseState.Error(
                            UserMessages.of(e, "Unknown error")
                        )
                    )
                }
            }
        }
    }

}