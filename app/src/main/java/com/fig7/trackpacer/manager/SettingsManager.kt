package com.fig7.trackpacer.manager

import com.fig7.trackpacer.data.SettingsData
import org.json.JSONException
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

    fun setPowerStart(powerStart: Boolean) {
        Save it first, if ok, change the value
        If not, report save failure
        Then make power start work!!

        settingsData.powerStart.value = powerStart
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
        settingsData.powerStart.value = (defaultSettings[0] == "true")
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
                "powerStart" -> settingsData.powerStart.value = json.getString(key).toBoolean()
            }
        }
    }

    private fun writeData() {
        val json = JSONObject()

        try {
            // 1.0
            json.put("powerStart", settingsData.powerStart.value.toString())
        } catch (e: JSONException) {
            return
        }

        val settingsFile = File(settingsDir, "settings.dat")
        try {
            val writer = BufferedWriter(FileWriter(settingsFile))
            writer.write(json.toString())
            writer.close()
        } catch (e: Exception) {
            return
        }
    }

    private fun updateData() {
        writeData()
        writeVersion()
    }
}
