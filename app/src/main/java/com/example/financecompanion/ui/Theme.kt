package com.example.financecompanion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightAppColorScheme = lightColorScheme(
    primary = PrimaryGreen,

    background = White,
    onBackground = Black,

    surface = White,
    onSurface = Black,

    onSurfaceVariant = Grey,

)

private val DarkAppColorScheme = darkColorScheme(
    primary = PrimaryGreen,

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,

    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun FinanceCompanionTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkAppColorScheme else LightAppColorScheme,
        content = content
    )
}
