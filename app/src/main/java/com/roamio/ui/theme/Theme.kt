package com.roamio.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Paper,
    primaryContainer = Forest,
    onPrimaryContainer = Mint,
    secondary = Forest,
    onSecondary = Paper,
    secondaryContainer = Cream,
    onSecondaryContainer = Forest,
    tertiary = StarYellow,
    onTertiary = Forest,
    background = Sage,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Muted,
    outline = Muted,
    error = Color(0xFFB3261E),
)

/**
 * Applies the Roamio sage and forest theme.
 *
 * @param content Composable content wrapped by the theme.
 * @author udit
 */
@Composable
fun RoamioTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
