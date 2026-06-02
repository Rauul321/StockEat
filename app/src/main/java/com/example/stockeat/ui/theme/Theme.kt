package com.example.stockeat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GreenPastelMain,
    onPrimary = GreenDarkText,
    primaryContainer = Color(0xFF2D4F2D),
    onPrimaryContainer = GreenPastelLight,

    secondary = GreenPastelLight,
    onSecondary = GreenDarkText,
    secondaryContainer = Color(0xFF233B23),
    onSecondaryContainer = GreenPastelUltraLight,

    background = Color(0xFF1A1C19),
    onBackground = GreenPastelUltraLight,

    surface = Color(0xFF1A1C19),
    onSurface = GreenPastelUltraLight,

    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = GreenPastelLight,

    outline = Color(0xFF8C9388)
)

private val LightColorScheme = lightColorScheme(
    // Colores principales
    primary = GreenPastelMain,
    onPrimary = GreenDarkText,
    primaryContainer = GreenPastelUltraLight,
    onPrimaryContainer = GreenDarkText,

    // Colores secundarios
    secondary = GreenPastelLight,
    onSecondary = GreenDarkText,
    secondaryContainer = GreenPastelLight,
    onSecondaryContainer = GreenDarkText,

    // Fondos y Superficies
    background = WhiteBackground,
    onBackground = GreenDarkText,
    surface = WhitePure,
    onSurface = GreenDarkText,
    
    // Variantes para Cards y TextField
    surfaceVariant = WhitePure,
    onSurfaceVariant = GreenDarkText,
    
    // Otros
    outline = SoftOutline,
    outlineVariant = SoftOutline,
    tertiary = GreenPastelLight,
    tertiaryContainer = GreenPastelUltraLight,
    onTertiaryContainer = GreenDarkText
)

@Composable
fun StockEatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
