package com.fig7.trackpacer.ui.run

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class RunViewModel(private val state: SavedStateHandle): ViewModel() {
    var newTimeIndex = 0

    private val mutableSelectedDist = MutableLiveData<Int>(state["dist"])
    val selectedDist: LiveData<Int> get() = mutableSelectedDist

    private val mutableSelectedLane = MutableLiveData<Int>(state["lane"])
    val selectedLane: LiveData<Int> get() = mutableSelectedLane

    private val mutableSelectedTime = MutableLiveData<Int>(state["time"])
    val selectedTime: LiveData<Int> get() = mutableSelectedTime

    fun selectDist(distIndex: Int) {
        if(mutableSelectedDist.value != distIndex) {
            mutableSelectedDist.value = distIndex
            state["dist"] = mutableSelectedDist.value
        }
    }

    fun selectLane(laneIndex: Int) {
        if(mutableSelectedLane.value != laneIndex) {
            mutableSelectedLane.value = laneIndex
            state["lane"] = mutableSelectedLane.value
        }
    }

    fun selectTime(timeIndex: Int) {
        if(mutableSelectedTime.value != timeIndex) {
            mutableSelectedTime.value = timeIndex
            state["time"] = mutableSelectedTime.value
        }
    }

    fun resetDist(newTimeIndex: Int) {
        this.newTimeIndex = newTimeIndex
        mutableSelectedDist.value = mutableSelectedDist.value
    }
}