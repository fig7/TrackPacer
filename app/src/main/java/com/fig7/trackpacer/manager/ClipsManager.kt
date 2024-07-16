package com.fig7.trackpacer.manager

import android.content.res.Resources
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.fig7.trackpacer.R
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Locale

private const val clipVersion = "1.0"
private val defaultClipMap = linkedMapOf(
    "Get ready"   to mutableListOf(Pair("On your marks",    Pair(R.raw.threetwoone, "")),
                                   Pair("Set",              Pair(R.raw.go, ""))),

    "Start"       to mutableListOf(Pair("321Go!",           Pair(R.raw.threetwoone, "")),
                                   Pair("321Go! (tts)",     Pair(-1, "{ \"locale\":\"Default\", \"voice\":\"\", \"rate\":\"1.0\", \"text\":\"3 2 1 Go!\" }"))),

    "Quick start" to mutableListOf(Pair("Go!",         Pair(R.raw.go, "")),
                                   Pair("Go! (tts)",   Pair(-1, "{ \"locale\":\"Default\", \"voice\":\"\", \"rate\":\"1.0\", \"text\":\"Go!\" }"))),

    "Waypoints"   to mutableListOf(Pair("50m",              Pair(R.raw.fifty, "")),
                                   Pair("100m",             Pair(R.raw.onehundred, "")),
                                   Pair("150m",             Pair(R.raw.onehundredandfifty, "")),
                                   Pair("200m",             Pair(R.raw.twohundred, "")),
                                   Pair("250m",             Pair(R.raw.twohundredandfifty, "")),
                                   Pair("300m",             Pair(R.raw.threehundred, "")),
                                   Pair("350m",             Pair(R.raw.threehundredandfifty, ""))),

    "Laps"        to mutableListOf(Pair("Lap 2",            Pair(R.raw.lap2, "")),
                                   Pair("Lap 3",            Pair(R.raw.lap3, "")),
                                   Pair("Lap 4",            Pair(R.raw.lap4, "")),
                                   Pair("Lap 5",            Pair(R.raw.lap5, "")),
                                   Pair("Lap 6",            Pair(R.raw.lap6, "")),
                                   Pair("Lap 7",            Pair(R.raw.lap7, "")),
                                   Pair("Lap 8",            Pair(R.raw.lap8, "")),
                                   Pair("Lap 9",            Pair(R.raw.lap9, "")),
                                   Pair("Lap 10",           Pair(R.raw.lap10, "")),
                                   Pair("Lap 11",           Pair(R.raw.lap11, "")),
                                   Pair("Lap 12",           Pair(R.raw.lap12, "")),
                                   Pair("Lap 13",           Pair(R.raw.lap13, "")),
                                   Pair("Lap 14",           Pair(R.raw.lap14, "")),
                                   Pair("Lap 15",           Pair(R.raw.lap15, "")),
                                   Pair("Lap 16",           Pair(R.raw.lap16, "")),
                                   Pair("Lap 17",           Pair(R.raw.lap17, "")),
                                   Pair("Lap 18",           Pair(R.raw.lap18, "")),
                                   Pair("Lap 19",           Pair(R.raw.lap19, "")),
                                   Pair("Lap 20",           Pair(R.raw.lap20, "")),
                                   Pair("Lap 21",           Pair(R.raw.lap21, "")),
                                   Pair("Lap 22",           Pair(R.raw.lap22, "")),
                                   Pair("Lap 23",           Pair(R.raw.lap23, "")),
                                   Pair("Lap 24",           Pair(R.raw.lap24, "")),
                                   Pair("Lap 25",           Pair(R.raw.lap25, ""))),

  "Distance"      to mutableListOf(Pair("50m",              Pair(R.raw.fifty, "")),
                                   Pair("100m",             Pair(R.raw.onehundred, "")),
                                   Pair("150m",             Pair(R.raw.onehundredandfifty, "")),
                                   Pair("200m",             Pair(R.raw.twohundred, "")),
                                   Pair("250m",             Pair(R.raw.twohundredandfifty, "")),
                                   Pair("300m",             Pair(R.raw.threehundred, "")),
                                   Pair("350m",             Pair(R.raw.threehundredandfifty, "")),
                                   Pair("400m",             Pair(R.raw.fourhundred, ""))),

  "Profile"       to mutableListOf(Pair("Stop and wait",    Pair(R.raw.fifty, "")),
                                   Pair("30 seconds",       Pair(R.raw.fifty, "")),
                                   Pair("10 seconds",       Pair(R.raw.fifty, "")),
                                   Pair("Speed up",         Pair(R.raw.fifty, "")),
                                   Pair("Slow down",        Pair(R.raw.fifty, ""))),

  "Motivation"    to mutableListOf(),

  "Finish"        to mutableListOf(Pair("Finish line",      Pair(R.raw.finish, ""))),

  "Status"        to mutableListOf(Pair("Pacing paused",    Pair(R.raw.paused, "")),
                                   Pair("Pacing cancelled", Pair(R.raw.cancelled, "")),
                                   Pair("Pacing complete",  Pair(R.raw.complete, ""))))

class ClipsManager(filesDir: File) {
    private val clipDir: File = File(filesDir, "Clips")
    private lateinit var currentVersion: String

    private val clipMap = linkedMapOf<String, SnapshotStateList<File>>()

    fun initClips(resources: Resources) {
        if(clipDir.exists()) {
            readVersion()
            readData()

            if(currentVersion != clipVersion) {
                updateData()
            }
        } else {
            initData(resources)
        }
    }

    fun clipsForCat(clipCat: String): SnapshotStateList<File> {
        return clipMap[clipCat] ?: throw IllegalArgumentException()
    }

    private fun readVersion() {
        val versionFile = File(clipDir, "version.dat")
        if (!versionFile.exists()) { throw IOException() }

        currentVersion = versionFile.readText()
    }

    private fun writeVersion() {
        val versionFile = File(clipDir, "version.dat")
        versionFile.writeText(clipVersion)
    }

    private fun InputStream.toFile(file: File) {
        file.outputStream().use { this.copyTo(it) }
    }

    private fun initData(resources: Resources) {
        if(!clipDir.mkdir()) throw IOException()

        var i = 0
        for(entry in defaultClipMap) {
            val clipCat = entry.key
            val clipFolderName = String.format(Locale.ROOT, "Cat_%03d_%s", i++, clipCat)
            val clipCatDir     = File(clipDir, clipFolderName)
            if(!clipCatDir.mkdir()) throw IOException()

            var clipList = clipMap[clipCat]
            if(clipList == null) {
                clipList = mutableStateListOf()
                clipMap[clipCat] = clipList
            }

            val defaultClipList = entry.value
            for(clipPair in defaultClipList) {
                val clipFile: File
                val clipMP3 = (clipPair.second.second == "")
                if(clipMP3) {
                    clipFile = File(clipCatDir, clipPair.first + ".m4a")

                    val resStream  = resources.openRawResource(clipPair.second.first)
                    resStream.toFile(clipFile)
                    resStream.close()
                } else {
                    clipFile = File(clipCatDir, clipPair.first + ".tts")

                    val outputData = clipPair.second.second
                    clipFile.writeText(outputData)
                }

                clipList.add(clipFile)
            }
        }

        writeVersion()
    }

    private fun readData() {
        val folderList = clipDir.list() ?: throw IOException()
        val catFilter = { clipCat: String -> clipCat.startsWith("Cat") }

        val folderArray = folderList.filter(catFilter).toTypedArray()
        folderArray.sort()

        for(catFolder in folderArray) {
            val catDir  = File(clipDir, catFolder)
            val clipCat = catFolder.substring(8)

            var clipList = clipMap[clipCat]
            if(clipList == null) {
                clipList = mutableStateListOf()
                clipMap[clipCat] = clipList
            }

            val catFiles = catDir.listFiles() ?: throw IOException()
            for(catFile in catFiles) {
                clipList.add(catFile)
            }
        }
    }

    private fun updateData() {
        throw IllegalArgumentException()
    }
}