package com.fig7.trackpacer.util

import android.content.res.Resources
import com.fig7.trackpacer.R
import kotlin.math.abs

fun timeToString(resources: Resources, timeInMS: Long): String {
    var timeLeft = abs(timeInMS)
    val sgnStr   = if (timeInMS < 0) "-" else ""

    var hrs = timeLeft / 3600000L
    timeLeft -= hrs * 3600000L

    var mins = timeLeft / 60000L
    timeLeft -= mins * 60000L

    var secs = timeLeft / 1000L
    timeLeft -= secs * 1000L

    if (((hrs > 0L) || (mins > 0L)) && (timeLeft > 0L)) {
        secs += 1L
        if (secs == 60L) {
            secs = 0L
            mins += 1L
            if (mins == 60L) {
                mins = 0L
                hrs += 1L
            }
        }
    }

    return if (hrs > 0L) {
        val hrsStr  = String.format("%d", hrs)
        val minsStr = String.format("%02d", mins)
        val secsStr = String.format("%02d", secs)
        resources.getString(R.string.base_time_hms, sgnStr, hrsStr, minsStr, secsStr)
    } else if (mins > 0L) {
        val minsStr = String.format("%d", mins)
        val secsStr = String.format("%02d", secs)
        resources.getString(R.string.base_time_ms, sgnStr, minsStr, secsStr)
    } else {
        val secsStr = String.format("%d", secs)
        val msStr = String.format("%03d", timeLeft)
        resources.getString(R.string.base_time_s, sgnStr, secsStr, msStr)
    }
}

fun timeToMinuteString(resources: Resources, timeInMS: Long): String {
    var timeLeft = abs(timeInMS)
    val sgnStr   = if (timeInMS < 0) "-" else ""

    var hrs = timeLeft / 3600000L
    timeLeft -= hrs * 3600000L

    var mins = timeLeft / 60000L
    timeLeft -= mins * 60000L

    var secs = timeLeft / 1000L
    timeLeft -= secs * 1000L

    secs += 1L
    if (secs == 60L) {
        secs = 0L
        mins += 1L
        if (mins == 60L) {
            mins = 0L
            hrs += 1L
        }
    }

    return if (hrs > 0L) {
        val hrsStr  = String.format("%d", hrs)
        val minsStr = String.format("%02d", mins)
        val secsStr = String.format("%02d", secs)
        resources.getString(R.string.base_time_hms, sgnStr, hrsStr, minsStr, secsStr)
    } else {
        val minsStr = String.format("%d", mins)
        val secsStr = String.format("%02d", secs)
        resources.getString(R.string.base_time_ms, sgnStr, minsStr, secsStr)
    }
}

fun timeToAlmostFullString(resources: Resources, timeInMS: Long): String {
    var timeLeft = abs(timeInMS)
    val sgnStr   = if (timeInMS < 0) "-" else ""

    val hrs = timeLeft / 3600000L
    timeLeft -= hrs * 3600000L

    val mins = timeLeft / 60000L
    timeLeft -= mins * 60000L

    val secs = timeLeft / 1000L
    timeLeft -= secs * 1000L

    val msStr = String.format("%02d", timeLeft/10L)
    return if(hrs > 0) {
        val hrsStr = String.format("%d", hrs)
        val minsStr = String.format("%02d", mins)
        val secsStr = String.format("%02d", secs)
        resources.getString(R.string.base_time_all, sgnStr, hrsStr, minsStr, secsStr, msStr)
    } else {
        val minsStr = String.format("%d", mins)
        val secsStr = String.format("%02d", secs)
        resources.getString(R.string.base_time_mss, sgnStr, minsStr, secsStr, msStr)
    }
}

fun timeToFullString(resources: Resources, timeInMS: Long): String {
    var timeLeft = abs(timeInMS)
    val sgnStr   = if (timeInMS < 0) "-" else ""

    val hrs = timeLeft / 3600000L
    val hrsStr = String.format("%02d", hrs)
    timeLeft -= hrs * 3600000L

    val mins = timeLeft / 60000L
    val minsStr = String.format("%02d", mins)
    timeLeft -= mins * 60000L

    val secs = timeLeft / 1000L
    val secsStr = String.format("%02d", secs)
    timeLeft -= secs * 1000L

    val msStr = String.format("%03d", timeLeft)
    return resources.getString(R.string.base_time_all, sgnStr, hrsStr, minsStr, secsStr, msStr)
}
