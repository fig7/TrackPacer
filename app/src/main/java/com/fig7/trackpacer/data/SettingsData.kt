package com.fig7.trackpacer.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class SettingsData(
    var startDelay: String                    = "5.00",
    var powerStart: MutableState<Boolean>     = mutableStateOf(false),
    var quickStart: MutableState<Boolean>     = mutableStateOf(false),
    var alternateStart: MutableState<Boolean> = mutableStateOf(false)
)
