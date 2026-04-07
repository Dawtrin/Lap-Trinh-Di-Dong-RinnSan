package com.rinnsan.noteapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light color scheme — Apple Flagship 2026 ──────────────────
private val LightColorScheme = lightColorScheme(
    primary          = AppleBlue,
    onPrimary        = AppleWhite,
    primaryContainer = AppleBlueLight,
    onPrimaryContainer = AppleBlueDark,

    secondary        = AppleGray600,
    onSecondary      = AppleWhite,
    secondaryContainer = AppleGray200,
    onSecondaryContainer = AppleBlack,

    tertiary         = ApplePurple,
    onTertiary       = AppleWhite,

    background       = AppleGray100,
    onBackground     = AppleBlack,

    surface          = AppleWhite,
    onSurface        = AppleBlack,
    surfaceVariant   = AppleGray200,
    onSurfaceVariant = AppleGray600,

    outline          = AppleGray300,
    outlineVariant   = AppleGray200,

    error            = AppleRed,
    onError          = AppleWhite,
    errorContainer   = Color(0xFFFFE5E3),
    onErrorContainer = Color(0xFF8B0000),

    inverseSurface   = AppleBlack,
    inverseOnSurface = AppleGray100,
    inversePrimary   = Color(0xFF64B5F6),

    scrim            = Color(0x66000000),
    surfaceTint      = AppleBlue,
)

// ── Dark color scheme — Apple Flagship 2026 ───────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF4DA3FF),
    onPrimary        = Color(0xFF003B7A),
    primaryContainer = Color(0xFF0058B0),
    onPrimaryContainer = Color(0xFFD6E8FF),

    secondary        = AppleGray300,
    onSecondary      = DarkSurface,
    secondaryContainer = DarkSurface2,
    onSecondaryContainer = AppleGray200,

    tertiary         = Color(0xFFD09BF0),
    onTertiary       = Color(0xFF4A1A6B),

    background       = DarkBg,
    onBackground     = AppleWhite,

    surface          = DarkSurface,
    onSurface        = AppleWhite,
    surfaceVariant   = DarkSurface2,
    onSurfaceVariant = AppleGray400,

    outline          = DarkBorder,
    outlineVariant   = DarkSurface3,

    error            = Color(0xFFFF6B6B),
    onError          = Color(0xFF1A0000),
    errorContainer   = Color(0xFF7F0000),
    onErrorContainer = Color(0xFFFFB3B3),

    inverseSurface   = AppleGray100,
    inverseOnSurface = AppleBlack,
    inversePrimary   = AppleBlue,

    scrim            = Color(0x99000000),
    surfaceTint      = Color(0xFF4DA3FF),
)

@Composable
fun NoteAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent status bar
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
