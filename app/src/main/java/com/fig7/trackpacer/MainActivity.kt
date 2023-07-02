package com.fig7.trackpacer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var waypointService: WaypointService
    private lateinit var distanceAndTimeArray: Array<String>
    private lateinit var timerView: TextView
    private var pacingInProgress = false

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startPacing()
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()

            val spinner1 = findViewById<Spinner>(R.id.spinner_distance)
            val spinner2 = findViewById<Spinner>(R.id.spinner_time)
            if (waypointService.beginPacing(spinner1.selectedItem.toString(), spinner2.selectedItem.toString())) {
                pacingInProgress = true

                val goButton = findViewById<ImageButton>(R.id.button_go)
                goButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.pause))

                val stopButton = findViewById<ImageButton>(R.id.button_stop)
                stopButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.stop))
                stopButton.isEnabled = true
                stopButton.isClickable = true

                timerView.text = getString(R.string.base_time, "00", "00", "00", "000")
                handler.postDelayed(runnable, 100)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        handleTimeUpdate()
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

        timerView = findViewById(R.id.text_time)
        timerView.text = getString(R.string.base_time, "00", "00", "00", "000")

        val goButton = findViewById<ImageButton>(R.id.button_go)
        goButton.setOnClickListener {
            if (!pacingInProgress) {
                beginRun()
            } else {
                pauseRun()
            }
        }

        val stopButton = findViewById<ImageButton>(R.id.button_stop)
        stopButton.setOnClickListener {
            stopRun()
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

    private fun handleTimeUpdate() {
        var elapsedTime = waypointService.elapsedTime()
        val hrs = elapsedTime / 3600000L
        val hrsStr = String.format("%02d", hrs)
        elapsedTime -= hrs * 3600000L

        val mins = elapsedTime / 60000L
        val minsStr = String.format("%02d", mins)
        elapsedTime -= mins * 60000L

        val secs = elapsedTime / 1000L
        val secsStr = String.format("%02d", secs)
        elapsedTime -= secs * 1000L

        val ms = elapsedTime
        val msStr = String.format("%03d", ms)

        timerView.text = getString(R.string.base_time, hrsStr, minsStr, secsStr, msStr)
        handler.postDelayed(runnable, 100)
    }

    private fun beginRun() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                startPacing()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // showInContextUI(...)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startPacing() {
        val intent = Intent(this, WaypointService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun pauseRun() {
        pacingInProgress = false

        val goButton = findViewById<ImageButton>(R.id.button_go)
        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))

        val stopButton = findViewById<ImageButton>(R.id.button_stop)
        stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
        stopButton.isEnabled = false
        stopButton.isClickable = false

        unbindService(connection)
        handler.removeCallbacks(runnable)
    }

    private fun stopRun() {
        pacingInProgress = false

        val goButton = findViewById<ImageButton>(R.id.button_go)
        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))

        val stopButton = findViewById<ImageButton>(R.id.button_stop)
        stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
        stopButton.isEnabled = false
        stopButton.isClickable = false

        unbindService(connection)
        handler.removeCallbacks(runnable)
    }
}
