package com.disciplinedminds.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = TextOnSurfaceDark,
    secondary = SecondaryGreen,
    onSecondary = TextOnSurfaceDark,
    tertiary = TertiaryAmber,
    error = ErrorRedDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextOnSurfaceDark,
    onSurface = TextOnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = ColorTokens.White,
    secondary = SecondaryGreen,
    onSecondary = ColorTokens.White,
    tertiary = TertiaryAmber,
    error = ErrorRed,
    background = BackgroundLight,
    surface = ColorTokens.White,
    onBackground = TextOnSurfaceLight,
    onSurface = TextOnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = ColorTokens.MutedText
)

@Composable
fun DisciplinedMindsTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(6),
            small = RoundedCornerShape(10),
            medium = RoundedCornerShape(14),
            large = RoundedCornerShape(20),
            extraLarge = RoundedCornerShape(28)
        ),
        content = content
    )
}

private object ColorTokens {
    val White = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val MutedText = androidx.compose.ui.graphics.Color(0xFF6B7280)
}
