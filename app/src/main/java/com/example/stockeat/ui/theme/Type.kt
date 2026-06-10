package com.example.stockeat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.stockeat.R


var myfont = FontFamily(
    Font(R.font.quicksand)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 80.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 48.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 36.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.Light,
        fontSize = 10.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.12.sp
    ),
    labelMedium = TextStyle(
        fontFamily = myfont,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
