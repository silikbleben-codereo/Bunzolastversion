package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BunzoDarkColorScheme = darkColorScheme(
    primary = FlameOrange,
    onPrimary = Color.White,
    primaryContainer = FlameOrangeDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = AmberGold,
    onSecondary = Color(0xFF2E1500),
    secondaryContainer = Color(0xFF4D3800),
    onSecondaryContainer = Color(0xFFFFE08A),
    background = CharcoalDark,
    onBackground = Color(0xFFF5ECE5),
    surface = CharcoalSurface,
    onSurface = Color(0xFFF5ECE5),
    surfaceVariant = CharcoalBorder,
    onSurfaceVariant = Color(0xFFD4C7BE),
    outline = Color(0xFF524842),
    error = StatusCancelled,
    onError = Color.White
)

private val BunzoLightColorScheme = lightColorScheme(
    primary = FlameOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBE4),
    onPrimaryContainer = FlameOrangeDark,
    secondary = AmberGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3D6),
    onSecondaryContainer = Color(0xFF573B00),
    background = WarmCreamBg,
    onBackground = TextPrimary,
    surface = WarmWhite,
    onSurface = TextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFE2D6CB),
    error = StatusCancelled,
    onError = Color.White
)

@Composable
fun BunzoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BunzoDarkColorScheme else BunzoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
