package com.fig7.trackpacer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fig7.trackpacer.PacingStatus

class PacingModel: ViewModel() {
    var runDist = ""
    var runLaps = ""
    var runProf = ""
    var runLane = -1
    var runTime = -1.0
    var pausedTime = -1L

    private val mutablePacingStatus = MutableLiveData<PacingStatus>()
    val pacingStatus: LiveData<PacingStatus> get() = mutablePacingStatus

    private val mutableElapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = mutableElapsedTime

    private var mutableWaypointName = MutableLiveData<String>()
    val waypointName: LiveData<String> get() = mutableWaypointName

    private val mutableWaypointProgress = MutableLiveData<Double>()
    val waypointProgress: LiveData<Double> get() = mutableWaypointProgress

    private val mutableTimeRemaining = MutableLiveData<Long?>()
    val timeRemaining: LiveData<Long?> get() = mutableTimeRemaining

    private val mutableDistRun = MutableLiveData<Double>()
    val distRun: LiveData<Double> get() = mutableDistRun

    private val mutableServiceReady = MutableLiveData<Boolean>()
    val serviceReady: LiveData<Boolean> get() = mutableServiceReady

    init {
        setPacingStatus(PacingStatus.NotPacing)
    }

    fun setPacingStatus(pacingStatus: PacingStatus) {
        mutablePacingStatus.value = pacingStatus
    }

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

    fun setServiceReady(serviceReady: Boolean) {
        mutableServiceReady.value = serviceReady
    }
}