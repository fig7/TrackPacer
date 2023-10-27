package com.fig7.trackpacer.manager

import androidx.compose.runtime.mutableStateOf
import com.fig7.trackpacer.data.SettingsData
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

const val settingsVersion = "1.0"
class SettingsManager(filesDir: File) {
    private val settingsDir: File
    private lateinit var currentVersion: String

    lateinit var settingsData: SettingsData
    val startDelay: String
        get() = settingsData.startDelay.value

    val powerStart: Boolean
        get() = settingsData.powerStart.value

    val alternateStart: Boolean
        get() = settingsData.alternateStart.value

    init {
        settingsDir = File(filesDir, "Settings")
    }

    fun initSettings(defaultSettings: Array<String>) {
        if (settingsDir.exists()) {
            readVersion()

            settingsFromDefaults(defaultSettings)
            readData()

            if(currentVersion != settingsVersion) {
                updateData()
            }
        } else {
            initData(defaultSettings)
        }
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
        settingsData.startDelay.value     = (defaultSettings[0])
        settingsData.powerStart.value     = (defaultSettings[1] == "true")
        settingsData.alternateStart.value = (defaultSettings[2] == "true")
    }

    private fun initData(defaultSettings: Array<String>) {
        if(!settingsDir.mkdir()) throw IOException()

        settingsFromDefaults(defaultSettings)
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
                "startDelay"     -> settingsData.startDelay.value     = json.getString(key)
                "powerStart"     -> settingsData.powerStart.value     = json.getString(key).toBoolean()
                "alternateStart" -> settingsData.alternateStart.value = json.getString(key).toBoolean()
            }
        }
    }

    private fun writeData(newSettingsData: SettingsData = settingsData) {
        val json = JSONObject()

        // 1.0
        json.put("startDelay",     newSettingsData.startDelay.value)
        json.put("powerStart",     newSettingsData.powerStart.value.toString())
        json.put("alternateStart", newSettingsData.alternateStart.value.toString())

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
