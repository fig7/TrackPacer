package com.fig7.trackpacer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fig7.trackpacer.manager.StorageManager
import com.fig7.trackpacer.R

class StorageModel(application: Application): AndroidViewModel(application) {
    val storageManager = StorageManager(application.filesDir)
    var storageDataOK = true

    init {
        try {
            storageManager.initDistances(application.resources.getStringArray(R.array.distance_array))
        } catch (_: Exception) {
            storageDataOK = false
        }
    }
}
