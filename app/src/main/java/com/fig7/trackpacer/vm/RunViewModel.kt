package com.fig7.trackpacer.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fig7.trackpacer.DataErrorDialog
import com.fig7.trackpacer.DataManager
import com.fig7.trackpacer.R

class RunViewModel(application: Application): AndroidViewModel(application) {
    val dataManager = DataManager(application.filesDir)
    var dataManagerOK = true

    private val mutableSelectedTime = MutableLiveData<Int>()
    val selectedTime: LiveData<Int> get() = mutableSelectedTime

    init {
        try {
            dataManager.initDistances(application.resources.getStringArray(R.array.distance_array))
        } catch (_: Exception) {
            dataManagerOK = false
        }
    }

    fun selectTime(timeIndex: Int) {
        mutableSelectedTime.value = timeIndex
    }
}