package com.fig7.trackpacer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fig7.trackpacer.manager.HistoryManager

class HistoryModel(application: Application): AndroidViewModel(application) {
    val historyManager = HistoryManager(application.filesDir)
    var historyDataOK = true

    init {
        try {
            historyManager.initHistory()
        } catch (_: Exception) {
            historyDataOK = false
        }
    }

    fun loadHistory() {
        try {
            if(historyDataOK) historyManager.loadHistory()
        } catch (_: Exception) {
            historyDataOK = false
        }
    }
}

