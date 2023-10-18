package com.fig7.trackpacer.data

import android.os.Parcelable
import com.fig7.trackpacer.tpVersion
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResultData(
    var runVersion: String = tpVersion,
    var resultUUID: String = "",

    var runDate: Long   = 0L,
    var runDist: String = "",
    var runLane: Int    = -1,
    var runProf: String = "",

    var totalDistStr: String = "",
    var totalTimeStr: String = "",
    var totalPaceStr: String = "",

    var actualTimeStr: String = "",
    var actualPaceStr: String = "",
    var earlyLateStr:  String = "",

    var runNotes: String = ""
): Parcelable

