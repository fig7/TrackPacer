package com.fig7.trackpacer.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class SettingsData(
    var powerStart: MutableState<Boolean> = mutableStateOf(false)
)
