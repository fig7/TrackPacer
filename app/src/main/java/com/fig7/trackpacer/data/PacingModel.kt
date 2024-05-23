package com.fig7.trackpacer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fig7.trackpacer.util.Int64

class PacingModel: ViewModel() {
    var runDist = ""
    var runLaps = ""
    var runProf = ""
    var runLane = -1
    var runTime = -1.0
    var alternateStart = false

    var totalDist = -1.0

    var totalDistStr = ""
    var totalTimeStr = ""
    var totalPaceStr = ""

    private val mutableElapsedTime = MutableLiveData<Int64>()
    val elapsedTime: LiveData<Int64> get() = mutableElapsedTime
    val elapsedTimeL: Int64 get() = mutableElapsedTime.value!!

    private var mutableWaypointName = MutableLiveData<String>()
    val waypointName: LiveData<String> get() = mutableWaypointName

    private val mutableWaypointProgress = MutableLiveData<Double>()
    val waypointProgress: LiveData<Double> get() = mutableWaypointProgress

    private val mutableTimeRemaining = MutableLiveData<Int64?>()
    val timeRemaining: LiveData<Int64?> get() = mutableTimeRemaining

    private val mutableDistRun = MutableLiveData<Double>()
    val distRun: LiveData<Double> get() = mutableDistRun

    fun setElapsedTime(elapsedTime: Int64) {
        mutableElapsedTime.value = elapsedTime
    }

    fun setDistRun(distRun: Double) {
        mutableDistRun.value = distRun
    }

    fun setWaypointProgress(waypointName: String, waypointProgress: Double, timeRemaining: Int64) {
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