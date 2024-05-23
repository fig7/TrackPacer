package com.fig7.trackpacer.data

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.fig7.trackpacer.R
import com.fig7.trackpacer.util.Int64
import com.fig7.trackpacer.waypoint.timeFor
import com.fig7.trackpacer.util.timeToAlmostFullString
import com.fig7.trackpacer.util.timeToMinuteString
import com.fig7.trackpacer.util.timeToString

class ResultModel: ViewModel() {
    var resultData = ResultData()

    fun initPacingResult(startTime: Int64, pacingModel: PacingModel) {
        resultData.runDate = startTime

        resultData.runDist   = pacingModel.runDist
        resultData.runLane   = pacingModel.runLane
        resultData.runProf   = pacingModel.runProf

        resultData.totalDistStr = pacingModel.totalDistStr
        resultData.totalTimeStr = pacingModel.totalTimeStr
        resultData.totalPaceStr = pacingModel.totalPaceStr
    }

    fun setPacingResult(resources: Resources, pacingModel: PacingModel) {
        val actualTime = pacingModel.elapsedTimeL
        resultData.actualTimeStr = timeToAlmostFullString(resources, actualTime)

        val actualPace = (1000.0 * actualTime) / pacingModel.totalDist
        resultData.actualPaceStr = timeToMinuteString(resources, actualPace.toLong())

        val totalTime = timeFor(pacingModel.runDist, pacingModel.runLane, pacingModel.runTime)
        var timeDiff = actualTime - totalTime.toLong()
        if (timeDiff <= -1000L) {
            timeDiff = -timeDiff

            val timeDiffRes = if (timeDiff  < 60000L) R.string.completion_seconds_early else R.string.completion_early
            resultData.earlyLateStr = resources.getString(timeDiffRes, timeToString(resources, timeDiff))
        } else if (timeDiff > 2000L) {
            val timeDiffRes = if (timeDiff  < 60000L) R.string.completion_seconds_late else R.string.completion_late
            resultData.earlyLateStr = resources.getString(timeDiffRes, timeToString(resources, timeDiff))
        } else {
            resultData.earlyLateStr = "Perfect pacing!"
        }
    }
}
