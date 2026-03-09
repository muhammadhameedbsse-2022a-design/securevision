package com.securevision.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Cyan500,
    onPrimary = Navy900,
    primaryContainer = Navy600,
    onPrimaryContainer = Cyan100,
    secondary = Navy300,
    onSecondary = White,
    secondaryContainer = Navy700,
    onSecondaryContainer = Navy100,
    tertiary = Green500,
    onTertiary = Navy900,
    tertiaryContainer = Navy800,
    onTertiaryContainer = Green400,
    error = Red500,
    errorContainer = Red200,
    onError = White,
    onErrorContainer = Red500,
    background = Navy900,
    onBackground = Grey100,
    surface = Surface800,
    onSurface = Grey200,
    surfaceVariant = Surface700,
    onSurfaceVariant = Grey300,
    outline = Surface600,
    outlineVariant = Surface700,
    scrim = Navy900,
    inverseSurface = Grey200,
    inverseOnSurface = Navy900,
    inversePrimary = Navy500
)

private val LightColorScheme = lightColorScheme(
    primary = Navy500,
    onPrimary = White,
    primaryContainer = Navy50,
    onPrimaryContainer = Navy900,
    secondary = Navy300,
    onSecondary = White,
    secondaryContainer = Navy50,
    onSecondaryContainer = Navy700,
    tertiary = Green500,
    onTertiary = White,
    tertiaryContainer = Grey100,
    onTertiaryContainer = Navy900,
    error = Red500,
    errorContainer = Red100,
    onError = White,
    onErrorContainer = Red500,
    background = Grey100,
    onBackground = Navy900,
    surface = White,
    onSurface = Navy900,
    surfaceVariant = Grey200,
    onSurfaceVariant = Surface700,
    outline = Grey400,
    outlineVariant = Grey300,
    scrim = Navy900,
    inverseSurface = Surface800,
    inverseOnSurface = Grey200,
    inversePrimary = Cyan400
)

@Composable
fun SecureVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SecureVisionTypography,
        content = content
    )
}
