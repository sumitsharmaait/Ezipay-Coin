package com.app.ezipaycoin.data.remote.dto

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    val titleLine1: String,
    val titleLine2: String,
    val titleLine1Color: Color = Color.White,
    val titleLine2Color: Color
)
