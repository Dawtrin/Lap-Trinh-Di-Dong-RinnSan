package com.example.cupcake.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Accent500,
    onPrimary          = Color.White,
    primaryContainer   = Accent100,
    onPrimaryContainer = AccentDark,
    secondary          = Neutral700,
    onSecondary        = Color.White,
    background         = SurfaceWarm,
    onBackground       = Neutral900,
    surface            = SurfaceCard,
    onSurface          = Neutral900,
    surfaceVariant     = Neutral100,
    onSurfaceVariant   = Neutral400,
    outline            = Neutral200,
    outlineVariant     = Neutral200,
)

private val DarkColorScheme = darkColorScheme(
    primary            = Accent400,
    onPrimary          = Neutral900,
    primaryContainer   = AccentDark,
    onPrimaryContainer = Accent100,
    secondary          = Neutral200,
    onSecondary        = Neutral900,
    background         = Color(0xFF121212),
    onBackground       = Color(0xFFF0EFED),
    surface            = Color(0xFF1E1E1E),
    onSurface          = Color(0xFFF0EFED),
    surfaceVariant     = Neutral800,
    onSurfaceVariant   = Neutral400,
    outline            = Neutral700,
    outlineVariant     = Neutral700,
)

@Composable
fun CupcakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}