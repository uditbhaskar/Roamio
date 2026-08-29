package com.roamio.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6F7F5),
    onPrimaryContainer = Ink,
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2F2F2),
    onSecondaryContainer = Ink,
    tertiary = Teal,
    onTertiary = Color.White,
    background = Canvas,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = InkMuted,
    outline = Hairline,
    error = Color(0xFFB3261E),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Night,
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color.White,
    secondary = Teal,
    onSecondary = Night,
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = Color.White,
    tertiary = Teal,
    onTertiary = Night,
    background = Night,
    onBackground = Color.White,
    surface = NightSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = NightMuted,
    outline = Color(0xFF3D3D3D),
    error = Color(0xFFF2B8B5),
)

/**
 * Applies the Roamio product theme.
 *
 * @param darkTheme Whether to use the dark color scheme.
 * @param content Composable content wrapped by the theme.
 * @author udit
 */
@Composable
fun RoamioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
