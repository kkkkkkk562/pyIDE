package com.example.kotlinpythonide.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IdeColors = darkColorScheme(
    primary = Color(0xFF70D6C4),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF155148),
    onPrimaryContainer = Color(0xFF91F3E0),
    secondary = Color(0xFFF5C36A),
    secondaryContainer = Color(0xFF5B4518),
    onSecondaryContainer = Color(0xFFFFDFA0),
    tertiary = Color(0xFFB6C6FF),
    tertiaryContainer = Color(0xFF32466F),
    onTertiaryContainer = Color(0xFFDCE2FF),
    background = Color(0xFF101419),
    surface = Color(0xFF171D23),
    surfaceVariant = Color(0xFF20282F),
    onSurface = Color(0xFFE5E9ED),
    onSurfaceVariant = Color(0xFFB7C1C9),
    outlineVariant = Color(0xFF3A454D),
)

@Composable
fun IdeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IdeColors,
        typography = Typography(),
        content = content,
    )
}