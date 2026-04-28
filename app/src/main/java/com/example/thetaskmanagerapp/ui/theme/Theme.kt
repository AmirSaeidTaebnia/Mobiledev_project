package com.example.thetaskmanagerapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SlatePrimaryDark,
    secondary = SlateSecondaryDark,
    tertiary = SlateTertiaryDark,
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    primaryContainer = Color(0xFF334955),
    onPrimaryContainer = Color(0xFFD3E5F1)
)

private val LightColorScheme = lightColorScheme(
    primary = SlatePrimary,
    secondary = SlateSecondary,
    tertiary = SlateTertiary,
    
    // Explicitly defining containers to avoid default pink/purple
    primaryContainer = Color(0xFFD3E5F1), // Soft Blue-Grey
    onPrimaryContainer = Color(0xFF001E2E),
    secondaryContainer = Color(0xFFDDE3EA),
    onSecondaryContainer = Color(0xFF191C1E),
    tertiaryContainer = Color(0xFFE7E0EB),
    onTertiaryContainer = Color(0xFF1F1A24),
    
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    
    background = Color(0xFFFDFCFF),
    surface = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F)
)

@Composable
fun TheTaskManagerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always disabled to keep our sober theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}