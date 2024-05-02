package com.fig7.trackpacer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fig7.trackpacer.manager.DistanceManager
import com.fig7.trackpacer.R

class DistanceModel(application: Application): AndroidViewModel(application) {
    val distanceManager = DistanceManager(application.filesDir)
    var distanceDataOK = true

    init {
        try {
            distanceManager.initDistances(application.resources.getStringArray(R.array.distance_array))
        } catch (_: Exception) {
            distanceDataOK = false
        }
    }
}
