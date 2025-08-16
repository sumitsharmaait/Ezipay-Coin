package com.app.ezipaycoin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.ezipaycoin.R

// Set of Material typography styles to start with
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(400),
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(400),
        fontSize = 22.sp,
        lineHeight = 50.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(600),
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(400),
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 12.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(600),
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(700),
        fontSize = 40.sp,
        lineHeight = 56.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.archivo_light)),
        fontWeight = FontWeight(700),
        fontSize = 34.sp,
        lineHeight = 56.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)