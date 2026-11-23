package com.example.airteldatamonitor.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AirtelRed,
    onPrimary = Color.White,
    primaryContainer = DarkCardVariant,
    onPrimaryContainer = DarkTextPrimary,
    secondary = StatusOnline,
    onSecondary = DarkCanvas,
    secondaryContainer = DarkCardElevated,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = StatusInfo,
    onTertiary = DarkCanvas,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder,
    error = StatusError,
    onError = DarkTextPrimary,
    errorContainer = Color(0x33EF4444),
    onErrorContainer = StatusError
)

private val LightColorScheme = lightColorScheme(
    primary = AirtelRed,
    onPrimary = Color.White,
    primaryContainer = LightCardVariant,
    onPrimaryContainer = LightTextPrimary,
    secondary = StatusOnline,
    onSecondary = Color.White,
    secondaryContainer = LightCardElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = StatusInfo,
    onTertiary = Color.White,
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder,
    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0x1AEF4444),
    onErrorContainer = StatusError
)

@Composable
fun AirtelDataMonitorTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
