package com.aistudio.poysha.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PoyshaPrimary,
    onPrimary = Color.White,
    primaryContainer = PoyshaPrimaryLight,
    onPrimaryContainer = Color.White,
    secondary = PoyshaSecondary,
    onSecondary = Color.White,
    background = PoyshaBg,
    onBackground = PoyshaTextDark,
    surface = PoyshaCardBg,
    onSurface = PoyshaTextDark,
    error = PoyshaRed,
    onError = Color.White
)

@Composable
fun PoyshaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
