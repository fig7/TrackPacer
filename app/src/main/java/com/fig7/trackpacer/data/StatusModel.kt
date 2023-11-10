package com.fig7.trackpacer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fig7.trackpacer.enums.PacingStatus

class StatusModel: ViewModel() {
    var startDelay     = ""
    var powerStart     = false
    var quickStart     = false

    private val mutablePacingStatus = MutableLiveData<PacingStatus>()
    val pacingStatus: LiveData<PacingStatus> get() = mutablePacingStatus

    init {
        setPacingStatus(PacingStatus.NotPacing)
    }

    fun setPacingStatus(pacingStatus: PacingStatus) {
        mutablePacingStatus.value = pacingStatus
    }
}