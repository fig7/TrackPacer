package com.fig7.trackpacer.ui.run

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RunViewModel: ViewModel() {
    private val mutableSelectedDist = MutableLiveData(0)
    val selectedDist: LiveData<Int> get() = mutableSelectedDist

    private val mutableSelectedLane = MutableLiveData(0)
    val selectedLane: LiveData<Int> get() = mutableSelectedLane

    private val mutableSelectedTime = MutableLiveData(0)
    val selectedTime: LiveData<Int> get() = mutableSelectedTime

    fun selectDist(distIndex: Int) {
        mutableSelectedDist.value = distIndex
    }

    fun selectLane(laneIndex: Int) {
        mutableSelectedLane.value = laneIndex
    }

    fun selectTime(timeIndex: Int) {
        mutableSelectedTime.value = timeIndex
    }
}