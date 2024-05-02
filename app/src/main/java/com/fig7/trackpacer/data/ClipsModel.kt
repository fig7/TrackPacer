package com.fig7.trackpacer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fig7.trackpacer.R
import com.fig7.trackpacer.manager.ClipsManager

class ClipsModel(application: Application): AndroidViewModel(application) {
    val clipsManager = ClipsManager(application.filesDir)
    var clipsDataOK = true

    init {
        try {
            clipsManager.initClips(application.resources)
        } catch (_: Exception) {
            clipsDataOK = false
        }
    }
}
