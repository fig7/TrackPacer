package com.fig7.trackpacer

import java.io.File

class DataManager(filesDir: File, defaultDistances: Array<String>) {
    private val dataDir: File
    var dataOk = false

    lateinit var distanceArray: Array<String>
    var timeMap = mutableMapOf<String, Array<String>>()

    private fun writeData() {
        for (distance in distanceArray) {
            val distanceDir = File(dataDir, distance)
            if (!distanceDir.exists()) {
                dataOk = distanceDir.mkdir()
                if (!dataOk) { return }
            }

            val timesFile = File(distanceDir, "times.dat")
            val timeString = timeMap[distance]!!.joinToString(separator = ",")
            timesFile.writeText(timeString)
        }

    }

    private fun readData() {

    }

    private fun initData(distances: Array<String>) {
        distanceArray = Array(distances.size) { distances[it].split("+")[0] }
        for ((i, runDistance) in distanceArray.withIndex()) {
            timeMap[runDistance] = distances[i].split("+")[1].trim().split(",").toTypedArray()
        }

        dataOk = dataDir.mkdir()
        if (dataOk) { writeData() }
    }

    init {
        dataDir = File(filesDir, "Data")
        if (dataDir.exists()) {
            readData()
        } else {
            initData(defaultDistances)
        }
    }

    private fun timeGreaterThan(time1: String, time2: String): Boolean {
        val time1Split = time1.split(":")
        val time1Dbl = 1000.0*(time1Split[0].trim().toLong()*60.0 + time1Split[1].toDouble())

        val time2Split = time2.split(":")
        val time2Dbl = 1000.0*(time2Split[0].trim().toLong()*60.0 + time2Split[1].toDouble())
        return time1Dbl > time2Dbl
    }

    fun deleteTime(runDistance: String, runTime: String?) {
        if (runTime == null) { return }
        if (!timeMap.containsKey(runDistance)) { return }

        val timeArray = timeMap[runDistance]!!
        if (!timeArray.contains(runTime)) return

        var i = 0
        val newTimeArray = Array(timeArray.size-1) { "" }
        for (time in timeArray) {
            if (time == runTime) {
                continue
            }

            newTimeArray[i++] = time
        }

        timeMap[runDistance] = newTimeArray
    }

    fun addTime(runDistance: String, runTime: String?) {
        if (runTime == null) { return }
        if (!timeMap.containsKey(runDistance)) { return }

        var i = 0
        var j = 0
        val timeArray = timeMap[runDistance]!!
        val newTimeArray = Array(timeArray.size+1) { "" }
        while (i < timeArray.size) {
            val time = timeArray[i]
            if (timeGreaterThan(time, runTime)) {
                break
            }

            i++
            newTimeArray[j++] = time
        }

        newTimeArray[j++] = runTime

        while (i < timeArray.size) {
            val time = timeArray[i++]
            newTimeArray[j++] = time
        }

        timeMap[runDistance] = newTimeArray
    }

    fun replaceTime(runDistance: String, origTime: String?, newTime: String?) {
        if ((origTime == null) || (newTime == null)) { return }
        if (!timeMap.containsKey(runDistance)) { return }

        var i = 0
        var j = 0
        val timeArray = timeMap[runDistance]!!
        val newTimeArray = Array(timeArray.size) { "" }
        while (i < timeArray.size) {
            val time = timeArray[i]
            if (timeGreaterThan(time, newTime)) {
                break
            }

            i++
            if (time == origTime) {
                continue
            }

            newTimeArray[j++] = time
        }

        newTimeArray[j++] = newTime

        while (i < timeArray.size) {
            val time = timeArray[i]

            ++i
            if (time == origTime) {
                continue
            }

            newTimeArray[j++] = time
        }

        timeMap[runDistance] = newTimeArray
    }
}