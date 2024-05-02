package com.fig7.trackpacer.ui.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.fig7.trackpacer.manager.ClipsManager

data class TabItem(val title: String, val icon: ImageVector?, val screen: @Composable (clipsManager: ClipsManager) -> Unit)
