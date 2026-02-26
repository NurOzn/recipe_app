package com.melikenurozun.recipe_app.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen, // Lighter green for dark mode
    secondary = ChampagneGold,
    tertiary = OliveGreen,
    background = DeepCoffee,
    surface = MediumCoffee,
    onPrimary = DeepCoffee,
    onSecondary = DeepCoffee,
    onTertiary = RichCream,
    onBackground = RichCream,
    onSurface = RichCream,
    surfaceVariant = MediumCoffee,
    onSurfaceVariant = RichCream.copy(alpha = 0.7f),
    error = MutedRed
)

private val LightColorScheme = lightColorScheme(
    primary = OliveGreen,
    secondary = ChampagneGold,
    tertiary = SageGreen,
    background = WarmCream,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = CharcoalBlack,
    onTertiary = PureWhite,
    onBackground = CharcoalBlack,
    onSurface = CharcoalBlack,
    surfaceVariant = SoftBeige, // Slightly warmer variant
    onSurfaceVariant = WarmGray,
    error = MutedRed
)

@Composable
fun Recipe_appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our palette
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
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}