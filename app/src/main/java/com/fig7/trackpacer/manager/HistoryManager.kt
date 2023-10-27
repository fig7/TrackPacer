package com.fig7.trackpacer.manager

import androidx.compose.runtime.mutableStateListOf
import com.fig7.trackpacer.data.ResultData
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.UUID

class HistoryManager(filesDir: File) {
    private val historyDir: File
    private var historyModified: FileTime? = null
    val historyList = mutableStateListOf<ResultData>()

    init {
        historyDir = File(filesDir, "History")
    }

    fun initHistory() {
        if (!historyDir.exists() && !historyDir.mkdir()) throw IOException()
    }

    fun loadHistory() {
        val historyDirAttr = Files.readAttributes(historyDir.toPath(), BasicFileAttributes::class.java)
        val lastModified = historyDirAttr.lastModifiedTime()
        if(lastModified.equals(historyModified)) {
            return
        }

        val folderList = historyDir.list() ?: throw IOException()
        historyList.clear()

        for (resultUUID in folderList) {
            val resultFile = File(historyDir, resultUUID)
            val resultText = resultFile.readText()

            val json = JSONObject(resultText)
            val keys: Iterator<String> = json.keys()
            val resultData = ResultData()
            for (key in keys) {
                when (key) {
                    "runVersion" -> resultData.runVersion = json.getString(key)

                    "runDate" ->    resultData.runDate = json.getString(key).toLong()
                    "runDist"   ->  resultData.runDist = json.getString(key)
                    "runLane"   ->  resultData.runLane = json.getString(key).toInt()
                    "runProf"   ->  resultData.runProf = json.getString(key)

                    "totalDist" -> resultData.totalDistStr = json.getString(key)
                    "totalTime" -> resultData.totalTimeStr = json.getString(key)
                    "totalPace" -> resultData.totalPaceStr = json.getString(key)

                    "actualTime" -> resultData.actualTimeStr = json.getString(key)
                    "actualPace" -> resultData.actualPaceStr = json.getString(key)
                    "earlyLate"  -> resultData.earlyLateStr  = json.getString(key)

                    "runNotes" -> resultData.runNotes = json.getString(key)
                }
            }

            resultData.resultUUID = resultUUID
            historyList.add(resultData)
        }

        historyList.sortWith(compareByDescending { it.runDate })
        historyModified = lastModified
    }

    fun saveHistory(resultData: ResultData): Boolean {
        val json = JSONObject()

        try {
            // 1.3
            json.put("runVersion", resultData.runVersion)

            json.put("runDate", resultData.runDate.toString())
            json.put("runDist", resultData.runDist)
            json.put("runLane", resultData.runLane.toString())
            json.put("runProf", resultData.runProf)

            json.put("totalDist", resultData.totalDistStr)
            json.put("totalTime", resultData.totalTimeStr)
            json.put("totalPace", resultData.totalPaceStr)

            json.put("actualTime", resultData.actualTimeStr)
            json.put("actualPace", resultData.actualPaceStr)
            json.put("earlyLate",  resultData.earlyLateStr)

            json.put("runNotes", resultData.runNotes)
        } catch (_: Exception) {
            return false
        }

        var historyFile: File
        var resultUUID: String
        do {
            resultUUID = UUID.randomUUID().toString()
            historyFile = File(historyDir, resultUUID)
        } while(historyFile.exists())

        try {
            val writer = BufferedWriter(FileWriter(historyFile))
            writer.write(json.toString())
            writer.close()
        } catch (_: Exception) {
            return false
        }

        return true
    }

    fun deleteHistory(resultIndex: Int): Boolean {
        val resultFile = File(historyDir, historyList[resultIndex].resultUUID)
        if(resultFile.delete()) {
            historyList.removeAt(resultIndex)
            return true
        }

        return false
    }
}