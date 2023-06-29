package com.fig7.trackpacer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var distanceAndTimeArray: Array<String>
    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(this, WaypointService::class.java)
            applicationContext.startForegroundService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        distanceAndTimeArray = resources.getStringArray(R.array.distance_array)
        val distanceArray: Array<String> = Array(distanceAndTimeArray.size) { distanceAndTimeArray[it].split("+")[0] }

        val spinner1 = findViewById<Spinner>(R.id.spinner_distance)
        val spinner1Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distanceArray)
        spinner1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = spinner1Adapter
        spinner1.onItemSelectedListener = this

        val spinner2 = findViewById<Spinner>(R.id.spinner_time)
        val timeArray: Array<String> = distanceAndTimeArray[0].split("+")[1].split(",").toTypedArray()
        val spinner2Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeArray)
        spinner2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = spinner2Adapter

        val button = findViewById<ImageButton>(R.id.button_go)
        button.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    val intent = Intent(this, WaypointService::class.java)
                    intent.putExtra("dist", spinner1.selectedItem.toString())
                    intent.putExtra("time", spinner2.selectedItem.toString())
                    applicationContext.startForegroundService(intent)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // showInContextUI(...)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        val timeArray: Array<String> = distanceAndTimeArray[pos].split("+")[1].split(",").toTypedArray()
        val spinner2 = findViewById<Spinner>(R.id.spinner_time)
        val spinner2Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeArray)
        spinner2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = spinner2Adapter
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }
}