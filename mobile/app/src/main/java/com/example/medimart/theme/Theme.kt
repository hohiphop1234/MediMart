package com.example.medimart.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

private val LightColorScheme = lightColorScheme(
    primary = MediMartOrange,
    onPrimary = MediMartWhite,
    primaryContainer = MediMartOrangeSoft,
    onPrimaryContainer = MediMartTextPrimary,
    secondary = MediMartOrangeDark,
    onSecondary = MediMartWhite,
    background = MediMartBg,
    onBackground = MediMartTextPrimary,
    surface = MediMartCardBg,
    onSurface = MediMartTextPrimary,
    surfaceVariant = MediMartBorder,
    error = MediMartRed,
    outline = MediMartBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = MediMartOrangeLight,
    onPrimary = MediMartTextPrimary,
    primaryContainer = MediMartOrange,
    onPrimaryContainer = MediMartWhite,
    secondary = MediMartOrangeDark,
    onSecondary = MediMartWhite,
    background = MediMartTextPrimary,
    onBackground = MediMartBg,
    surface = MediMartTextPrimary,
    onSurface = MediMartBg,
    error = MediMartRed
)

@Composable
fun MediMartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}
