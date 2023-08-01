package com.fig7.trackpacer

import java.io.File

class DataManager(filesDir: File, defaultDistances: Array<String>) {
    private val dataDir: File
    var dataOk = false

    lateinit var distanceArray: Array<String>
    var timeMap = mutableMapOf<String, Array<String>>()

    private fun writeData() {
        for ((i, distance) in distanceArray.withIndex()) {
            val prefix = String.format("Distance_%03d_", i)
            val distanceDir = File(dataDir, prefix + distance)
            if (!distanceDir.exists()) {
                dataOk = distanceDir.mkdir()
                if (!dataOk) { return }
            }

            val timesFile = File(distanceDir, "times.dat")
            val timeStr = timeMap[distance]!!.joinToString(separator = ",")
            timesFile.writeText(timeStr)
        }
    }

    private fun readData() {
        val distanceList = dataDir.list()
        if (distanceList == null) { dataOk = false; return }

        distanceList.sort()
        distanceArray = Array(distanceList.size) { "" }

        for ((i, distance) in distanceList.withIndex()) {
            val runDistance  = distance.substring(13)
            distanceArray[i] = runDistance

            val distanceDir = File(dataDir, distance)
            val timesFile = File(distanceDir, "times.dat")
            val timesStr  = timesFile.readText()
            timeMap[runDistance] = timesStr.split(",").toTypedArray()
        }
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

    fun deleteTime(runDistance: String, runTime: String?): Int {
        if (runTime == null) { return -1 }
        if (!timeMap.containsKey(runDistance)) { return -1 }

        val timeArray = timeMap[runDistance]!!
        if (!timeArray.contains(runTime)) return -1

        var i = 0
        var newIndex = -1
        val newTimeArray = Array(timeArray.size-1) { "" }
        for (time in timeArray) {
            if (time == runTime) {
                newIndex = i-1
                if (newIndex < 0) newIndex = 0
                continue
            }

            newTimeArray[i++] = time
        }

        timeMap[runDistance] = newTimeArray
        writeData()

        return newIndex
    }

    fun addTime(runDistance: String, runTime: String?): Int {
        if (runTime == null) { return -1 }
        if (!timeMap.containsKey(runDistance)) { return -1 }

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

        val newIndex = j
        newTimeArray[j++] = runTime

        while (i < timeArray.size) {
            val time = timeArray[i++]
            newTimeArray[j++] = time
        }

        timeMap[runDistance] = newTimeArray
        writeData()

        return newIndex
    }

    fun replaceTime(runDistance: String, origTime: String?, newTime: String?): Int {
        if ((origTime == null) || (newTime == null)) { return -1 }
        if (!timeMap.containsKey(runDistance)) { return -1 }

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

        val newIndex = j
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
        writeData()

        return newIndex
    }
}