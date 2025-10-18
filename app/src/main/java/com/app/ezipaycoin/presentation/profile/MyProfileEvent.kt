package com.app.ezipaycoin.presentation.profile

import android.net.Uri
import java.io.File

sealed class MyProfileEvent {
    data class UpdateName(val name: String) : MyProfileEvent()
    data object LogOut : MyProfileEvent()
    data class ImageSelected(val imageUri: Uri, val imageFile: File): MyProfileEvent()
}