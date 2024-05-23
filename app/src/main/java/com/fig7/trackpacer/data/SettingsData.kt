package com.fig7.trackpacer.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.fig7.trackpacer.util.Bool

data class SettingsData(
    var startDelay:     String                = "5.00",
    var powerStart:     MutableState<Bool> = mutableStateOf(false),
    var quickStart:     MutableState<Bool> = mutableStateOf(false),
    var alternateStart: MutableState<Bool> = mutableStateOf(false),
    var flightMode:     MutableState<Bool> = mutableStateOf(true)
)
