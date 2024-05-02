package com.fig7.trackpacer.manager

import androidx.compose.runtime.mutableStateOf
import com.fig7.trackpacer.data.SettingsData
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

private const val settingsVersion = "1.0"
class SettingsManager(filesDir: File) {
    private val settingsDir: File
    private lateinit var currentVersion: String

    lateinit var settingsData: SettingsData

    val startDelay: String
        get() = settingsData.startDelay

    val powerStart: Boolean
        get() = settingsData.powerStart.value

    val quickStart: Boolean
        get() = settingsData.quickStart.value

    val alternateStart: Boolean
        get() = settingsData.alternateStart.value

    val flightModeReminder: Boolean
        get() = settingsData.flightMode.value

    init {
        settingsDir = File(filesDir, "Settings")
    }

    fun initSettings(defaultSettings: Array<String>) {
        settingsFromDefaults(defaultSettings)

        if (settingsDir.exists()) {
            readVersion()
            readData()

            if(currentVersion != settingsVersion) {
                updateData()
            }
        } else {
            initData()
        }
    }

    fun setStartDelay(startDelay: String): Boolean {
        try {
            val newSettingsData = settingsData.copy(startDelay = startDelay)
            writeData(newSettingsData)
        } catch(_: Exception) {
            return false
        }

        settingsData.startDelay = startDelay
        return true
    }

    fun setPowerStart(powerStart: Boolean): Boolean {
        try {
            val newSettingsData = settingsData.copy(powerStart = mutableStateOf(powerStart))
            writeData(newSettingsData)
        } catch(_: Exception) {
            return false
        }

        settingsData.powerStart.value = powerStart
        return true
    }

    fun setQuickStart(quickStart: Boolean): Boolean {
        try {
            val newSettingsData = settingsData.copy(quickStart = mutableStateOf(quickStart))
            writeData(newSettingsData)
        } catch(_: Exception) {
            return false
        }

        settingsData.quickStart.value = quickStart
        return true
    }

    fun setAlternateStart(alternateStart: Boolean): Boolean {
        try {
            val newSettingsData = settingsData.copy(alternateStart = mutableStateOf(alternateStart))
            writeData(newSettingsData)
        } catch(_: Exception) {
            return false
        }

        settingsData.alternateStart.value = alternateStart
        return true
    }

    fun setFlightMode(flightMode: Boolean): Boolean {
        // NB Flight mode reminder is not saved
        /* try {
            val newSettingsData = settingsData.copy(flightMode = mutableStateOf(flightMode))
            writeData(newSettingsData)
        } catch(_: Exception) {
            return false
        } */

        settingsData.flightMode.value = flightMode
        return true
    }

    private fun readVersion() {
        val versionFile = File(settingsDir, "version.dat")
        if (!versionFile.exists()) { throw IOException() }

        currentVersion = versionFile.readText()
    }

    private fun writeVersion() {
        val versionFile = File(settingsDir, "version.dat")
        versionFile.writeText(settingsVersion)
    }

    private fun settingsFromDefaults(defaultSettings: Array<String>) {
        settingsData = SettingsData()
        settingsData.startDelay           = defaultSettings[0]
        settingsData.powerStart.value     = (defaultSettings[1] == "true")
        settingsData.quickStart.value     = (defaultSettings[2] == "true")
        settingsData.alternateStart.value = (defaultSettings[3] == "true")
        settingsData.flightMode.value     = (defaultSettings[4] == "true")
    }

    private fun initData() {
        if(!settingsDir.mkdir()) throw IOException()

        writeData()
        writeVersion()
    }

    private fun readData() {
        val settingsFile = File(settingsDir, "settings.dat")
        val settingsText = settingsFile.readText()

        val json = JSONObject(settingsText)
        val keys: Iterator<String> = json.keys()
        for (key in keys) {
            when (key) {
                "startDelay"     -> settingsData.startDelay           = json.getString(key)
                "powerStart"     -> settingsData.powerStart.value     = json.getString(key).toBoolean()
                "quickStart"     -> settingsData.quickStart.value     = json.getString(key).toBoolean()
                "alternateStart" -> settingsData.alternateStart.value = json.getString(key).toBoolean()
                // "flightMode"     -> settingsData.flightMode.value     = json.getString(key).toBoolean()
            }
        }
    }

    private fun writeData(newSettingsData: SettingsData = settingsData) {
        val json = JSONObject()

        // 1.0
        json.put("startDelay",     newSettingsData.startDelay)
        json.put("powerStart",     newSettingsData.powerStart.value.toString())
        json.put("quickStart",     newSettingsData.quickStart.value.toString())
        json.put("alternateStart", newSettingsData.alternateStart.value.toString())
        // json.put("flightMode",     newSettingsData.flightMode.value.toString())

        val settingsFile = File(settingsDir, "settings.dat")
        val writer = BufferedWriter(FileWriter(settingsFile))
        writer.write(json.toString())
        writer.close()
    }

    private fun updateData() {
        writeData()
        writeVersion()
    }
}
