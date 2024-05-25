package com.fig7.trackpacer.manager

import com.fig7.trackpacer.util.Bool
import java.io.File
import java.io.IOException
import java.util.Locale

private const val distanceVersion = "1.3"
class DistanceManager(filesDir: File) {
    private val distanceDir: File = File(filesDir, "Data")

    lateinit var distanceArray: Array<String>
    var timeMap = mutableMapOf<String, Array<String>>()

    private lateinit var currentVersion: String

    fun initDistances(defaultDistances: Array<String>) {
        if(distanceDir.exists()) {
            readVersion()
            readData()

            if(currentVersion != distanceVersion) {
              updateData(defaultDistances)
            }
        } else {
            initData(defaultDistances)
        }
    }

    private fun readVersion() {
        val versionFile = File(distanceDir, "version.dat")
        if(!versionFile.exists()) {
            // Version before version file existed
            currentVersion = "1.2"
            return
        }

        currentVersion = versionFile.readText()
    }

    private fun writeVersion() {
        val versionFile = File(distanceDir, "version.dat")
        versionFile.writeText(distanceVersion)
    }

    private fun initData(defaultDistances: Array<String>) {
        val success = distanceDir.mkdir()
        if(!success) { throw IOException() }

        distanceArray = Array(defaultDistances.size) { i: Int -> defaultDistances[i].split("+")[0] }
        for((i, runDistance) in distanceArray.withIndex()) {
            timeMap[runDistance] = defaultDistances[i].split("+")[1].trim().split(",").toTypedArray()
        }

        writeData()
        writeVersion()
    }

    private fun readData() {
        val folderList = distanceDir.list() ?: throw IOException()
        val distanceFilter = { distance: String -> distance.startsWith("Distance") }

        val folderArray = folderList.filter(distanceFilter).toTypedArray()
        folderArray.sort()
        distanceArray = Array(folderArray.size) { "" }

        for ((i, distance) in folderArray.withIndex()) {
            val runDistance  = distance.substring(13)
            distanceArray[i] = runDistance

            val distanceDir = File(distanceDir, distance)
            val timesFile = File(distanceDir, "times.dat")
            val timesStr  = timesFile.readText()
            timeMap[runDistance] = timesStr.split(",").toTypedArray()
        }
    }

    private fun writeData(distance: String) {
        val i = distanceArray.indexOf(distance)
        if(i == -1) { throw IllegalArgumentException() }

        val prefix = String.format(Locale.ROOT, "Distance_%03d_", i)
        val distanceDir = File(distanceDir, prefix + distance)
        if(!distanceDir.exists()) {
            val success = distanceDir.mkdir()
            if(!success) { throw IOException() }
        }

        val timesFile = File(distanceDir, "times.dat")
        val timeStr = timeMap[distance]!!.joinToString(separator = ",")
        timesFile.writeText(timeStr)
    }

    private fun writeData() {
        for((i, distance) in distanceArray.withIndex()) {
            val prefix = String.format(Locale.ROOT, "Distance_%03d_", i)
            val distanceDir = File(distanceDir, prefix + distance)
            if(!distanceDir.exists()) {
                val success = distanceDir.mkdir()
                if(!success) { throw IOException() }
            }

            val timesFile = File(distanceDir, "times.dat")
            val timeStr = timeMap[distance]!!.joinToString(separator = ",")
            timesFile.writeText(timeStr)
        }
    }

    private fun updateData(defaultDistances: Array<String>) {
        when(currentVersion) {
            "1.2" -> {
                // 1.2 -> 1.3
                // Rename Distance_002_1200m ....
                var oldDistanceDir = File(distanceDir, "Distance_002_1200m")
                var newDistanceDir = File(distanceDir, "Distance_003_1200m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_003_1500m")
                newDistanceDir = File(distanceDir, "Distance_004_1500m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_004_2000m")
                newDistanceDir = File(distanceDir, "Distance_005_2000m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_005_3000m")
                newDistanceDir = File(distanceDir, "Distance_006_3000m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_006_4000m")
                newDistanceDir = File(distanceDir, "Distance_007_4000m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_007_5000m")
                newDistanceDir = File(distanceDir, "Distance_008_5000m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_008_10000m")
                newDistanceDir = File(distanceDir, "Distance_009_10000m")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                oldDistanceDir = File(distanceDir, "Distance_009_1 mile")
                newDistanceDir = File(distanceDir, "Distance_010_1 mile")
                if(!oldDistanceDir.renameTo(newDistanceDir)) throw IOException()

                // Update distance array
                distanceArray = Array(defaultDistances.size) { defaultDistances[it].split("+")[0] }
                timeMap["1000m"] = defaultDistances[2].split("+")[1].trim().split(",").toTypedArray()

                // Write out Distance_002_1000m
                val distanceDir = File(distanceDir, "Distance_002_1000m")
                if (!distanceDir.exists() && !distanceDir.mkdir()) throw IOException()

                val timesFile = File(distanceDir, "times.dat")
                val timeStr = timeMap["1000m"]!!.joinToString(separator = ",")
                timesFile.writeText(timeStr)
            }

            else -> throw IllegalArgumentException()
        }

        writeVersion()
    }

    private fun timeGreaterThan(time1: String, time2: String): Bool {
        val time1Split = time1.split(":")
        val time1Split0 = time1Split[0].trim().toLong()
        val time1Split1 = time1Split[1].toDouble()
        val time1Dbl = 1000.0*(time1Split0.toDouble()*60.0 + time1Split1)

        val time2Split = time2.split(":")
        val time2Split0 = time2Split[0].trim().toLong()
        val time2Split1 = time2Split[1].toDouble()
        val time2Dbl = 1000.0*(time2Split0.toDouble()*60.0 + time2Split1)
        return (time1Dbl > time2Dbl)
    }

    fun deleteTime(runDistance: String, runTime: String?): Int {
        if(runTime == null) { throw IllegalArgumentException() }

        val timeArray = timeMap[runDistance] ?: throw IllegalArgumentException()
        if(!timeArray.contains(runTime)) { throw IllegalArgumentException() }

        var i = 0
        var newIndex = -1
        val newTimeArray = Array(timeArray.size-1) { "" }
        for (time in timeArray) {
            if (time == runTime) {
                newIndex = i-1
                if (newIndex < 0) { newIndex = 0 }

                continue
            }

            newTimeArray[i] = time
            i += 1
        }

        timeMap[runDistance] = newTimeArray
        writeData(runDistance)

        return newIndex
    }

    fun addTime(runDistance: String, runTime: String?): Int {
        if (runTime == null) { throw IllegalArgumentException() }

        val timeArray = timeMap[runDistance] ?: throw IllegalArgumentException()
        if(timeArray.contains(runTime)) { throw IllegalArgumentException() }

        var i = 0
        var j = 0
        val newTimeArray = Array(timeArray.size+1) { "" }
        while(i < timeArray.size) {
            val time = timeArray[i]
            if(timeGreaterThan(time, runTime)) { break }

            newTimeArray[j] = time
            i += 1; j += 1
        }

        val newIndex = j
        newTimeArray[j] = runTime
        j += 1

        while(i < timeArray.size) {
            newTimeArray[j] = timeArray[i]
            i += 1; j += 1
        }

        timeMap[runDistance] = newTimeArray
        writeData(runDistance)

        return newIndex
    }

    fun replaceTime(runDistance: String, origTime: String?, newTime: String?): Int {
        if ((origTime == null) || (newTime == null)) { throw IllegalArgumentException() }

        val timeArray = timeMap[runDistance] ?: throw IllegalArgumentException()
        if(!timeArray.contains(origTime)) { throw IllegalArgumentException() }

        var i = 0
        var j = 0
        val newTimeArray = Array(timeArray.size) { "" }
        while(i < timeArray.size) {
            val time = timeArray[i]
            if(timeGreaterThan(time, newTime)) { break }

            i += 1
            if(time == origTime) { continue }

            newTimeArray[j] = time
            j += 1
        }

        val newIndex = j
        newTimeArray[j++] = newTime

        while (i < timeArray.size) {
            val time = timeArray[i]

            i += 1
            if(time == origTime) { continue }

            newTimeArray[j] = time
            j += 1
        }

        timeMap[runDistance] = newTimeArray
        writeData(runDistance)

        return newIndex
    }
}