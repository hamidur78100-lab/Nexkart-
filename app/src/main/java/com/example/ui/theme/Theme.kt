package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CharcoalColorScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = BlackPure,
    secondary = CyberCyan,
    onSecondary = BlackPure,
    tertiary = GoldCrown,
    background = DarkBackground,
    onBackground = WhitePure,
    surface = DarkSurface,
    onSurface = WhitePure,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = SlateGrey,
    error = AlertCoral
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CharcoalColorScheme,
        typography = Typography,
        content = content
    )
}
