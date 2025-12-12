package com.example.earnit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Light Schemes ---
private val PurpleLightScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    // Default containers are purple-ish by default in Material3
)

private val OceanLightScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = OceanPrimaryContainer, // Fixes Rewards Box
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF001F24),
    secondary = OceanSecondary,
    secondaryContainer = OceanSecondaryContainer, // Fixes Log Box
    tertiary = OceanTertiary,
    background = OceanBackground,
    surface = OceanSurface,
    surfaceContainer = OceanSurfaceContainer // Fixes Navigation Bar
)

private val NatureLightScheme = lightColorScheme(
    primary = NaturePrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = NaturePrimaryContainer,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF002106),
    secondary = NatureSecondary,
    secondaryContainer = NatureSecondaryContainer,
    tertiary = NatureTertiary,
    background = NatureBackground,
    surface = NatureSurface,
    surfaceContainer = NatureSurfaceContainer
)

private val SunsetLightScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = SunsetPrimaryContainer,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF410002),
    secondary = SunsetSecondary,
    secondaryContainer = SunsetSecondaryContainer,
    tertiary = SunsetTertiary,
    background = SunsetBackground,
    surface = SunsetSurface,
    surfaceContainer = SunsetSurfaceContainer
)

// --- Dark Schemes ---
private val PurpleDarkScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val OceanDarkScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    primaryContainer = OceanPrimaryContainerDark,
    secondary = OceanSecondaryDark,
    secondaryContainer = OceanSecondaryContainerDark,
    tertiary = OceanTertiaryDark,
    background = OceanBackgroundDark,
    surface = OceanSurfaceDark,
    surfaceContainer = OceanSurfaceContainerDark
)

private val NatureDarkScheme = darkColorScheme(
    primary = NaturePrimaryDark,
    primaryContainer = NaturePrimaryContainerDark,
    secondary = NatureSecondaryDark,
    secondaryContainer = NatureSecondaryContainerDark,
    tertiary = NatureTertiaryDark,
    background = NatureBackgroundDark,
    surface = NatureSurfaceDark,
    surfaceContainer = NatureSurfaceContainerDark
)

private val SunsetDarkScheme = darkColorScheme(
    primary = SunsetPrimaryDark,
    primaryContainer = SunsetPrimaryContainerDark,
    secondary = SunsetSecondaryDark,
    secondaryContainer = SunsetSecondaryContainerDark,
    tertiary = SunsetTertiaryDark,
    background = SunsetBackgroundDark,
    surface = SunsetSurfaceDark,
    surfaceContainer = SunsetSurfaceContainerDark
)

@Composable
fun EarnItTheme(
    themeIndex: Int = 0,
    darkModePreference: Int = 0, // 0=System, 1=Light, 2=Dark
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkModePreference) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (themeIndex) {
        1 -> if (darkTheme) OceanDarkScheme else OceanLightScheme
        2 -> if (darkTheme) NatureDarkScheme else NatureLightScheme
        3 -> if (darkTheme) SunsetDarkScheme else SunsetLightScheme
        else -> if (darkTheme) PurpleDarkScheme else PurpleLightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use the surface container color for the status bar to blend with app bars
            window.statusBarColor = colorScheme.background.toArgb()
            // Use the surface container color for the navigation bar
            window.navigationBarColor = colorScheme.surfaceContainer.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}