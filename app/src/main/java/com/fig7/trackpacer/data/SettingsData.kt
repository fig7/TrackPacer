package com.fig7.trackpacer.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class SettingsData(
    var startDelay: MutableState<String>      = mutableStateOf("5.00"),
    var powerStart: MutableState<Boolean>     = mutableStateOf(false),
    var alternateStart: MutableState<Boolean> = mutableStateOf(false)
)
