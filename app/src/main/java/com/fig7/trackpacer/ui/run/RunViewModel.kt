package com.fig7.trackpacer.ui.run

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RunViewModel: ViewModel() {
    private val mutableSelectedTime = MutableLiveData<Int>()
    val selectedTime: LiveData<Int> get() = mutableSelectedTime

    fun selectTime(timeIndex: Int) {
        mutableSelectedTime.value = timeIndex
    }
}