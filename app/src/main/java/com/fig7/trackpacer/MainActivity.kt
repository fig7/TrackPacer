package com.fig7.trackpacer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat

// States
// 1. Initial:   NotPacing                          Stop: Disabled Play:  Enabled

// 2a. Play  ->  NotPacing    -> PacingStart        Stop: Enabled  Play:  Disabled       Count up from -5000L + 3, 2, 1, Go!
// 2b.           PacingStart  -> Pacing             Stop: Enabled  Pause: Enabled        Elapsed time >= 0

// 3.  Stop1  -> PacingStart  -> NotPacing          Stop: Disabled Play:  Disabled       Pacing cancelled -> Stop: Disabled Play:  Enabled

// 4.  Stop2  -> Pacing       -> NotPacing          Stop: Disabled Play:  Disabled       Pacing complete  -> Stop: Disabled Play:  Enabled

// 5a. Pause  -> Pacing       -> PacingPaused       Stop: Disabled Play:  Disabled       Pacing paused    -> Stop: Enabled  Play:  Enabled
// 5b. Resume -> PacingPaused -> Pacing             Stop: Disabled Play:  Disabled       Pacing resumed   -> Stop: Enabled  Pause: Enabled


// Progress indicators (Next up: Total:)
// Resume pacing
// Auto pause on phone call, or other audio request?
// Error handling on start / convert to jetpack / code review / ship!
// Add history + set own times (edit times)

private const val cancelledClip = R.raw.cancelled
private const val pauseClip     = R.raw.paused
private const val completeClip  = R.raw.complete

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var waypointService: WaypointService
    private lateinit var distanceAndTimeArray: Array<String>
    private var pacingStatus = PacingStatus.NotPacing

    private lateinit var mpPacingCancelled: MediaPlayer
    private lateinit var mpPacingPaused: MediaPlayer
    private lateinit var mpPacingComplete: MediaPlayer

    private lateinit var goButton: ImageButton
    private lateinit var stopButton: ImageButton

    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner

    private lateinit var timerView: TextView

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            beginPacing()
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()

            if (waypointService.beginPacing(spinner1.selectedItem.toString(), spinner2.selectedItem.toString())) {
                pacingStatus = PacingStatus.PacingStart

                stopButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.stop))
                stopButton.isEnabled = true
                stopButton.isClickable = true

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

        spinner1 = findViewById(R.id.spinner_distance)
        val spinner1Adapter = ArrayAdapter(this, R.layout.spinner_item, distanceArray)
        spinner1Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner1.adapter = spinner1Adapter
        spinner1.onItemSelectedListener = this

        spinner2 = findViewById(R.id.spinner_time)
        val timeArray: Array<String> = distanceAndTimeArray[0].split("+")[1].split(",").toTypedArray()
        val spinner2Adapter = ArrayAdapter(this, R.layout.spinner_item, timeArray)
        spinner2Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner2.adapter = spinner2Adapter

        timerView = findViewById(R.id.text_time)
        timerView.text = getString(R.string.base_time, "", "00", "00", "00", "000")

        goButton = findViewById(R.id.button_go)
        goButton.setOnClickListener {
            when (pacingStatus) {
                PacingStatus.NotPacing    -> beginPacing()
                PacingStatus.PacingPaused -> resumePacing()
                PacingStatus.Pacing       -> pausePacing()
                else                      -> throw IllegalStateException()
            }
        }

        stopButton = findViewById(R.id.button_stop)
        stopButton.setOnClickListener {
            stopPacing()
        }

        mpPacingCancelled = MediaPlayer.create(this, cancelledClip)
        mpPacingCancelled.setOnCompletionListener {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true
        }

        mpPacingComplete = MediaPlayer.create(this, completeClip)
        mpPacingComplete.setOnCompletionListener {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true
        }

        mpPacingPaused = MediaPlayer.create(this, pauseClip)
        mpPacingPaused.setOnCompletionListener {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true

            stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop))
            stopButton.isEnabled = true
            stopButton.isClickable = true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        val timeArray: Array<String> = distanceAndTimeArray[pos].split("+")[1].split(",").toTypedArray()
        val spinner2Adapter = ArrayAdapter(this, R.layout.spinner_item, timeArray)
        spinner2Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner2.adapter = spinner2Adapter
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }

    private fun handleTimeUpdate() {
        var elapsedTime = waypointService.elapsedTime()
        var sgnStr = ""
        if (elapsedTime < 0) {
            sgnStr = "-"
            elapsedTime = -elapsedTime
        } else if (pacingStatus == PacingStatus.PacingStart) {
            pacingStatus = PacingStatus.Pacing

            goButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.pause))
            goButton.isClickable = true
            goButton.isEnabled = true
        }

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

        timerView.text = getString(R.string.base_time, sgnStr, hrsStr, minsStr, secsStr, msStr)
        handler.postDelayed(runnable, 100)
    }

    private fun beginPacing() {
        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                startService()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // showInContextUI(...)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun resumePacing() {
    }

    private fun startService() {
        val intent = Intent(this, WaypointService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun pausePacing() {
        pacingStatus = PacingStatus.PacingPaused

        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
        stopButton.isEnabled = false
        stopButton.isClickable = false

        unbindService(connection)
        handler.removeCallbacks(runnable)

        mpPacingPaused.start()
    }

    private fun stopPacing() {
        stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
        stopButton.isEnabled = false
        stopButton.isClickable = false

        if ((pacingStatus == PacingStatus.Pacing) || (pacingStatus == PacingStatus.PacingStart)) {
            unbindService(connection)
            handler.removeCallbacks(runnable)

            if (pacingStatus == PacingStatus.Pacing) {
                goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
                goButton.isEnabled = false
                goButton.isClickable = false

                mpPacingComplete.start()
            } else {
                mpPacingCancelled.start()
            }
        } else if (pacingStatus == PacingStatus.PacingPaused) {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
            goButton.isEnabled = false
            goButton.isClickable = false

            mpPacingComplete.start()
        }

        pacingStatus = PacingStatus.NotPacing
    }
}
