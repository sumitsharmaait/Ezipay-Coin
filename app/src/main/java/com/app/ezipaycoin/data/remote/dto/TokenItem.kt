package com.app.ezipaycoin.data.remote.dto

import androidx.annotation.DrawableRes

data class TokenItem(
    @DrawableRes val iconRes: Int,
    val iconText: String? = null, // For tokens like USD where you might just draw text in a circle
    val name: String,
    val price: String,
    val percentageChange: String,
    val isUp: Boolean,
    val usdValue: String
)
