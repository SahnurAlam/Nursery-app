package com.sahnurnursery.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MintLight,
    secondary = LeafGreen,
    background = DarkGreenBackground,
    surface = DarkGreenSurface
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = Emerald,
    background = LightBackground,
    surface = LightSurface
)

@Composable
fun SahnurNurseryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
