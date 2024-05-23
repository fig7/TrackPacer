package com.fig7.trackpacer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.fig7.trackpacer.util.Bool

private val darkColors  = darkColors()
private val lightColors = lightColors()

@Composable
fun TPTheme(darkTheme: Bool = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colors = if (darkTheme) darkColors else lightColors, content = content)
}