package com.fig7.trackpacer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PacingModel: ViewModel() {
    var runDist = ""
    var runLaps = ""
    var runProf = ""
    var runLane = -1
    var runTime = -1.0
    var alternateStart = false

    var pausedTime = -1L

    var totalDist = -1.0

    var totalDistStr = ""
    var totalTimeStr = ""
    var totalPaceStr = ""

    private val mutableElapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = mutableElapsedTime
    val elapsedTimeL: Long get() = mutableElapsedTime.value!!

    private var mutableWaypointName = MutableLiveData<String>()
    val waypointName: LiveData<String> get() = mutableWaypointName

    private val mutableWaypointProgress = MutableLiveData<Double>()
    val waypointProgress: LiveData<Double> get() = mutableWaypointProgress

    private val mutableTimeRemaining = MutableLiveData<Long?>()
    val timeRemaining: LiveData<Long?> get() = mutableTimeRemaining

    private val mutableDistRun = MutableLiveData<Double>()
    val distRun: LiveData<Double> get() = mutableDistRun

    fun setElapsedTime(elapsedTime: Long) {
        mutableElapsedTime.value = elapsedTime
    }

    fun setDistRun(distRun: Double) {
        mutableDistRun.value = distRun
    }

    fun setWaypointProgress(waypointName: String, waypointProgress: Double, timeRemaining: Long) {
        mutableWaypointName.value      = waypointName
        mutableWaypointProgress.value  = waypointProgress
        mutableTimeRemaining.value     = timeRemaining
    }

    fun resetWaypointProgress() {
        mutableWaypointName.value      = ""
        mutableWaypointProgress.value  = 0.0
        mutableTimeRemaining.value     = null
    }
}