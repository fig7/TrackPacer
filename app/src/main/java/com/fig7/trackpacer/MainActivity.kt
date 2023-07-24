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
import kotlin.math.min

// States
// 1. Initial:   NotPacing                          Stop: Disabled Play:  Enabled

// 2a. Play  ->  NotPacing    -> PacingStart        Stop: Enabled  Play:  Disabled       Count up from -5000L + 3, 2, 1, Go!
// 2b.           PacingStart  -> Pacing             Stop: Enabled  Pause: Enabled        Elapsed time >= 0

// 3.  Stop1  -> PacingStart  -> NotPacing          Stop: Disabled Play:  Disabled       Pacing cancelled -> Stop: Disabled Play:  Enabled

// 4.  Stop2  -> Pacing       -> NotPacing          Stop: Disabled Play:  Disabled       Pacing complete  -> Stop: Disabled Play:  Enabled

// 5a. Pause  -> Pacing       -> PacingPaused       Stop: Disabled Play:  Disabled       Pacing paused    -> Stop: Enabled  Play:  Enabled
// 5b. Resume -> PacingPaused -> Pacing             Stop: Disabled Play:  Disabled       Pacing resumed   -> Stop: Enabled  Pause: Enabled


// Auto pause on phone call, or other audio request?
// Error handling on start / convert to jetpack / code review / ship!
// Clip recording / replacement / Tabs
// Add history + set own times (edit times, and edit distances). With runpacer: could do set a point on a map and set the time (use GPS, eek!).

private const val cancelledClip = R.raw.cancelled
private const val pauseClip     = R.raw.paused
private const val completeClip  = R.raw.complete

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var waypointService: WaypointService
    private lateinit var distanceAndTimeArray: Array<String>

    private var pacingStatus = PacingStatus.NotPacing
    private var pausedTime: Long = -1

    private lateinit var mpPacingCancelled: MediaPlayer
    private lateinit var mpPacingPaused: MediaPlayer
    private lateinit var mpPacingComplete: MediaPlayer

    private lateinit var goButton: ImageButton
    private lateinit var stopButton: ImageButton

    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var spinner3: Spinner

    private lateinit var timerView: TextView

    private lateinit var nextUpLabel: TextView
    private lateinit var nextUpProgress : ProgressBar

    private lateinit var timeToLabel: TextView
    private lateinit var timeToProgress : ProgressBar

    private fun runTimeFromSpinner(): Double {
        val runTimeStr = spinner2.selectedItem.toString()
        val runTimeSplit = runTimeStr.split(":")
        return 1000.0*(runTimeSplit[0].trim().toLong()*60.0 + runTimeSplit[1].toLong())
    }

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            if (pacingStatus == PacingStatus.NotPacing) {
                beginPacing()
            } else { // PacingPaused
                resumePacing()
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()

            if (pacingStatus == PacingStatus.ServiceStart) {
                if (waypointService.beginPacing(spinner1.selectedItem.toString(), runTimeFromSpinner())) {
                    pacingStatus = PacingStatus.PacingStart

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.stop))
                    stopButton.isEnabled = true
                    stopButton.isClickable = true

                    nextUpLabel.text = getString(R.string.nextup, "")
                    nextUpProgress.progress = 0

                    timeToLabel.text = getString(R.string.timeto, "")
                    timeToProgress.progress = 0

                    handler.postDelayed(runnable, 100)
                }
            } else { // ServiceResume
                if (waypointService.resumePacing(spinner1.selectedItem.toString(), runTimeFromSpinner(), pausedTime)) {
                    pacingStatus = PacingStatus.PacingResume

                    handler.postDelayed(runnable, 100)
                }
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

        spinner3 = findViewById(R.id.spinner_profile)
        val profileArray: Array<String> = resources.getStringArray(R.array.profile_array)
        val spinner3Adapter = ArrayAdapter(this, R.layout.spinner_item, profileArray)
        spinner3Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner3.adapter = spinner3Adapter

        timerView = findViewById(R.id.text_time)
        timerView.text = getString(R.string.base_time_all, "00", "00", "00", "000")

        nextUpLabel = findViewById(R.id.nextup_label)
        nextUpLabel.text = getString(R.string.nextup, "")

        nextUpProgress = findViewById(R.id.nextup_progress)
        nextUpProgress.progress = 0

        timeToLabel = findViewById(R.id.timeto_label)
        timeToLabel.text = getString(R.string.timeto, "")

        timeToProgress = findViewById(R.id.timeto_progress)
        timeToProgress.progress = 0

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

            spinner1.isEnabled = true
            spinner1.isClickable = true

            spinner2.isEnabled = true
            spinner2.isClickable = true
        }

        mpPacingComplete = MediaPlayer.create(this, completeClip)
        mpPacingComplete.setOnCompletionListener {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true

            spinner1.isEnabled = true
            spinner1.isClickable = true

            spinner2.isEnabled = true
            spinner2.isClickable = true
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

    private fun timeToString(timeInMS: Long): String {
        var timeLeft = timeInMS

        val hrs = timeLeft / 3600000L
        timeLeft -= hrs * 3600000L

        val mins = timeLeft / 60000L
        timeLeft -= mins * 60000L

        val secs = timeLeft / 1000L
        timeLeft -= secs * 1000L

        return if (hrs > 0) {
            val hrsStr  = String.format("%d", hrs)
            val minsStr = String.format("%02d", mins)
            val secsStr = String.format("%02d", secs)
            getString(R.string.base_time_hms, hrsStr, minsStr, secsStr)
        } else if (mins > 0) {
            val minsStr = String.format("%d", mins)
            val secsStr = String.format("%02d", secs)
            getString(R.string.base_time_ms, minsStr, secsStr)
        } else {
            val secsStr = String.format("%d", secs)
            val msStr = String.format("%03d", timeLeft)
            getString(R.string.base_time_s, secsStr, msStr)
        }
    }

    private fun timeToFullString(timeInMS: Long): String {
        var timeLeft = timeInMS

        val hrs = timeLeft / 3600000L
        val hrsStr = String.format("%02d", hrs)
        timeLeft -= hrs * 3600000L

        val mins = timeLeft / 60000L
        val minsStr = String.format("%02d", mins)
        timeLeft -= mins * 60000L

        val secs = timeLeft / 1000L
        val secsStr = String.format("%02d", secs)
        timeLeft -= secs * 1000L

        val msStr = String.format("%03d", timeLeft)
        return getString(R.string.base_time_all, hrsStr, minsStr, secsStr, msStr)
    }

    private fun handleTimeUpdate() {
        var elapsedSgn = ""
        var elapsedTime = waypointService.elapsedTime()

        if (elapsedTime < 0) {
            elapsedSgn = "-"
            elapsedTime = -elapsedTime
        } else if ((pacingStatus == PacingStatus.PacingStart) || (pacingStatus == PacingStatus.PacingResume)) {
            pacingStatus = PacingStatus.Pacing
            waypointService.beginRun(elapsedTime)

            goButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.pause))
            goButton.isClickable = true
            goButton.isEnabled = true
        } else {
            nextUpLabel.text = getString(R.string.nextup, waypointService.waypointName())
            nextUpProgress.progress = (100.0*waypointService.waypointProgress(elapsedTime)).toInt()

            var remainingSgn = ""
            var remainingTime = waypointService.timeRemaining(elapsedTime)
            timeToProgress.progress = min(100, (100.0 - 100.0*(remainingTime / runTimeFromSpinner())).toInt())

            if (remainingTime < 0) {
                remainingSgn = "-"
                remainingTime = -remainingTime
            }

            val remainingStr = timeToString(remainingTime)
            timeToLabel.text = getString(R.string.timeto, getString(R.string.signed_time, remainingSgn, remainingStr))
        }

        val elapsedStr = timeToFullString(elapsedTime)
        timerView.text = getString(R.string.signed_time, elapsedSgn, elapsedStr)

        handler.postDelayed(runnable, 100)
    }

    private fun beginPacing() {
        pacingStatus = PacingStatus.ServiceStart

        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        spinner1.isEnabled = false
        spinner1.isClickable = false

        spinner2.isEnabled = false
        spinner2.isClickable = false

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
        pacingStatus = PacingStatus.ServiceResume

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

    private fun startService() {
        val intent = Intent(this, WaypointService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun pausePacing() {
        pacingStatus = PacingStatus.PacingPaused
        pausedTime = waypointService.elapsedTime()

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
