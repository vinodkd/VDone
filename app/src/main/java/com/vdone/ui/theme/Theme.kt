package com.vdone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6B6BB5),       // periwinkle
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E8F8),
    onPrimaryContainer = Color(0xFF2E2E7A),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9E9ED8),       // light periwinkle
    onPrimary = Color(0xFF1A1A35),
    background = Color(0xFF1A1A35),
    surface = Color(0xFF26263F),
)

@Composable
fun VDoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
