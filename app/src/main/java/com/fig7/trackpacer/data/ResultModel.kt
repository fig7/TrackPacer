package com.fig7.trackpacer.data

import androidx.lifecycle.ViewModel

class ResultModel: ViewModel() {
    var startTime = 0L
    var runDist   = ""
    var runLane   = -1
    var runProf   = ""

    var totalDistStr = ""
    var totalTimeStr = ""
    var totalPaceStr = ""

    var actualTimeStr = ""
    var actualPaceStr = ""
    var earlyLateStr  = ""
}
