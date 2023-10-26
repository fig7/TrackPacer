package com.fig7.trackpacer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fig7.trackpacer.R
import com.fig7.trackpacer.manager.SettingsManager

class SettingsModel(application: Application): AndroidViewModel(application) {
    val settingsManager = SettingsManager(application.filesDir)
    var settingsDataOK = true

    init {
        try {
            settingsManager.initSettings(application.resources.getStringArray(R.array.settings_array))
        } catch (_: Exception) {
            settingsDataOK = false
        }
    }
}
