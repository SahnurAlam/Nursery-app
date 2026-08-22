package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NurseryDarkPrimary,
    onPrimary = NurseryDarkOnPrimary,
    primaryContainer = NurseryDarkPrimaryContainer,
    onPrimaryContainer = NurseryDarkOnPrimaryContainer,
    secondary = NurseryDarkSecondary,
    onSecondary = NurseryDarkOnSecondary,
    secondaryContainer = NurseryDarkSecondaryContainer,
    onSecondaryContainer = NurseryDarkOnSecondaryContainer,
    tertiary = NurseryDarkTertiary,
    onTertiary = NurseryDarkOnTertiary,
    tertiaryContainer = NurseryDarkTertiaryContainer,
    onTertiaryContainer = NurseryDarkOnTertiaryContainer,
    background = NurseryDarkBackground,
    onBackground = NurseryDarkOnBackground,
    surface = NurseryDarkSurface,
    onSurface = NurseryDarkOnSurface,
    surfaceVariant = NurseryDarkSurfaceVariant,
    onSurfaceVariant = NurseryDarkOnSurfaceVariant,
    outline = NurseryDarkOutline,
    outlineVariant = NurseryDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = NurseryGreenPrimary,
    onPrimary = NurseryGreenOnPrimary,
    primaryContainer = NurseryGreenPrimaryContainer,
    onPrimaryContainer = NurseryGreenOnPrimaryContainer,
    secondary = NurseryGreenSecondary,
    onSecondary = NurseryGreenOnSecondary,
    secondaryContainer = NurseryGreenSecondaryContainer,
    onSecondaryContainer = NurseryGreenOnSecondaryContainer,
    tertiary = NurseryAmberTertiary,
    onTertiary = NurseryAmberOnTertiary,
    tertiaryContainer = NurseryAmberTertiaryContainer,
    onTertiaryContainer = NurseryAmberOnTertiaryContainer,
    background = NurseryLightBackground,
    onBackground = NurseryLightOnBackground,
    surface = NurseryLightSurface,
    onSurface = NurseryLightOnSurface,
    surfaceVariant = NurseryLightSurfaceVariant,
    onSurfaceVariant = NurseryLightOnSurfaceVariant,
    outline = NurseryLightOutline,
    outlineVariant = NurseryLightOutlineVariant
)

@Composable
fun SahnurNurseryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature green brand theme consistent
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
