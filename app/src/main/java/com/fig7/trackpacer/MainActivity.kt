package com.fig7.trackpacer

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
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


// Error handling on start / convert to jetpack / code review / ship!
// Clip recording / replacement / Tabs
// Add history + set own times (edit times, and edit distances). With Runpacer: could do set a point on a map and set the time (use GPS, eek!).

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

    private var broadcastReceiver = ActivityReceiver()

    private fun runTimeFromSpinner(): Double {
        val runTimeStr = spinner2.selectedItem.toString()
        val runTimeSplit = runTimeStr.split(":")
        return 1000.0*(runTimeSplit[0].trim().toLong()*60.0 + runTimeSplit[1].toLong())
    }

    private fun enableSpinners(enabled: Boolean) {
        spinner1.isEnabled   = enabled
        spinner1.isClickable = enabled

        spinner2.isEnabled   = enabled
        spinner2.isClickable = enabled

        spinner3.isEnabled   = enabled
        spinner3.isClickable = enabled
    }

    private val requestNotificationsLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            checkPhonePermission()
        } else {
            stopPacing(true)

            val dialog = InfoDialog.newDialog("Pacing not possible", "Notifications permission is required. Allow notifications if you wish to use TrackPacer.")
            dialog.show(supportFragmentManager, "CANNOT_FUNCTION_DIALOG")
        }
    }

    private val requestPhoneLauncher = registerForActivityResult(RequestPermission()) {
        // TODO: Show an icon on the UI for the state of automatic pausing (NB. refresh UI in onResume())
        // And also for current state (play, pause, stopped) + start delay
        // Oh, and add a settings page / tab
        startService()
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

        savedInstanceState?.run {
            pacingStatus = PacingStatus.values()[getInt("PACING_STATUS")]
            pausedTime = getLong("PAUSED_TIME")
        }

        distanceAndTimeArray = resources.getStringArray(R.array.distance_array)
        val distanceArray: Array<String> = Array(distanceAndTimeArray.size) { distanceAndTimeArray[it].split("+")[0] }

        spinner1 = findViewById(R.id.spinner_distance)
        val spinner1Adapter = ArrayAdapter(this, R.layout.spinner_item, distanceArray)
        spinner1Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner1.adapter = spinner1Adapter

        var spinner1Pos = 0
        savedInstanceState?.run { spinner1Pos = getInt("SPINNER1_VAL"); spinner1.setSelection(spinner1Pos)  }
        spinner1.onItemSelectedListener = this

        spinner2 = findViewById(R.id.spinner_time)
        val timeArray: Array<String> = distanceAndTimeArray[spinner1Pos].split("+")[1].split(",").toTypedArray()
        val spinner2Adapter = ArrayAdapter(this, R.layout.spinner_item, timeArray)
        spinner2Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner2.adapter = spinner2Adapter
        savedInstanceState?.run { spinner2.setSelection(getInt("SPINNER2_VAL")) }

        spinner3 = findViewById(R.id.spinner_profile)
        val profileArray: Array<String> = resources.getStringArray(R.array.profile_array)
        val spinner3Adapter = ArrayAdapter(this, R.layout.spinner_item, profileArray)
        spinner3Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner3.adapter = spinner3Adapter

        timerView = findViewById(R.id.text_time)
        timerView.text = savedInstanceState?.getString("TIMER_VAL") ?: getString(R.string.base_time_all, "00", "00", "00", "000")

        nextUpLabel = findViewById(R.id.nextup_label)
        nextUpLabel.text = savedInstanceState?.getString("NEXTUP_LABEL") ?: getString(R.string.nextup, "")

        nextUpProgress = findViewById(R.id.nextup_progress)
        nextUpProgress.progress = savedInstanceState?.getInt("NEXTUP_PROGRESS") ?: 0

        timeToLabel = findViewById(R.id.timeto_label)
        timeToLabel.text = savedInstanceState?.getString("TIMETO_LABEL") ?: getString(R.string.timeto, "")

        timeToProgress = findViewById(R.id.timeto_progress)
        timeToProgress.progress = savedInstanceState?.getInt("TIMETO_PROGRESS") ?: 0

        goButton = findViewById(R.id.button_go)
        goButton.setOnClickListener {
            when (pacingStatus) {
                PacingStatus.NotPacing    -> beginPacing()
                PacingStatus.PacingPaused -> resumePacing()
                PacingStatus.Pacing       -> pausePacing(false)
                else                      -> throw IllegalStateException()
            }
        }

        stopButton = findViewById(R.id.button_stop)
        stopButton.setOnClickListener {
            stopPacing(false)
        }

        when (pacingStatus) {
            PacingStatus.NotPacing -> {
                goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
                goButton.isEnabled = true
                goButton.isClickable = true

                stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
                stopButton.isEnabled = false
                stopButton.isClickable = false

                enableSpinners(true)
            }
            PacingStatus.PacingPaused -> {
                goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
                goButton.isEnabled = true
                goButton.isClickable = true

                stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop))
                stopButton.isEnabled = true
                stopButton.isClickable = true

                enableSpinners(false)
            }
            else -> throw IllegalStateException()
        }

        mpPacingCancelled = MediaPlayer.create(this, cancelledClip)
        mpPacingCancelled.setOnCompletionListener {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true

            enableSpinners(true)
        }

        mpPacingComplete = MediaPlayer.create(this, completeClip)
        mpPacingComplete.setOnCompletionListener {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true

            enableSpinners(true)
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

        supportFragmentManager.setFragmentResultListener("REQUEST_NOTIFICATIONS_DIALOG", this) { _: String, bundle: Bundle ->
            val resultVal = bundle.getBoolean("RequestResult")
            if (resultVal) requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else stopPacing(true)
        }

        supportFragmentManager.setFragmentResultListener("REQUEST_PHONE_DIALOG", this) { _: String, bundle: Bundle ->
            val resultVal = bundle.getBoolean("RequestResult")
            if (resultVal) requestPhoneLauncher.launch(Manifest.permission.READ_PHONE_STATE) else stopPacing(true)
        }

        registerReceiver(broadcastReceiver, IntentFilter("TrackPacer.PAUSE_PACING"))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putInt("PACING_STATUS", pacingStatus.ordinal)
            putLong("PAUSED_TIME", pausedTime)

            putInt("SPINNER1_VAL", spinner1.selectedItemPosition)
            putInt("SPINNER2_VAL", spinner2.selectedItemPosition)
            putInt("SPINNER3_VAL", spinner3.selectedItemPosition)

            putString("TIMER_VAL", timerView.text.toString())

            putString("NEXTUP_LABEL", nextUpLabel.text.toString())
            putInt("NEXTUP_PROGRESS", nextUpProgress.progress)

            putString("TIMETO_LABEL", timeToLabel.text.toString())
            putInt("TIMETO_PROGRESS", timeToProgress.progress)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
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
        pacingStatus = PacingStatus.CheckPermissionStart

        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        enableSpinners(false)

        checkNotificationsPermission()
    }

    private fun resumePacing() {
        pacingStatus = PacingStatus.CheckPermissionResume

        goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        checkNotificationsPermission()
    }

    private fun startService() {
        pacingStatus = if (pacingStatus == PacingStatus.CheckPermissionStart) PacingStatus.ServiceStart else PacingStatus.ServiceResume

        val intent = Intent(this, WaypointService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun pausePacing(silent: Boolean) {
        pacingStatus = PacingStatus.PacingPaused
        pausedTime = waypointService.elapsedTime()

        unbindService(connection)
        handler.removeCallbacks(runnable)

        if (silent) {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled   = true
            goButton.isClickable = true

            stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop))
            stopButton.isEnabled   = true
            stopButton.isClickable = true
        } else {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
            goButton.isEnabled   = false
            goButton.isClickable = false

            stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
            stopButton.isEnabled   = false
            stopButton.isClickable = false

            mpPacingPaused.start()
        }
    }

    private fun stopPacing(silent: Boolean) {
        stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
        stopButton.isEnabled   = false
        stopButton.isClickable = false

        if (silent) {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled   = true
            goButton.isClickable = true

            enableSpinners(true)
        } else {
            goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
            goButton.isEnabled   = false
            goButton.isClickable = false
        }

        if ((pacingStatus == PacingStatus.Pacing) || (pacingStatus == PacingStatus.PacingStart)) {
            unbindService(connection)
            handler.removeCallbacks(runnable)

            if (!silent) {
                val mp = if (pacingStatus == PacingStatus.Pacing) mpPacingComplete else mpPacingCancelled
                mp.start()
            }
        } else if ((pacingStatus == PacingStatus.PacingPaused) && !silent) {
            mpPacingComplete.start()
        }

        pacingStatus = PacingStatus.NotPacing
    }

    fun handleIncomingCall() {
        when (pacingStatus) {
            PacingStatus.NotPacing    -> return
            PacingStatus.PacingPaused -> return
            PacingStatus.Pacing       -> pausePacing(true)
            else                      -> stopPacing(true)
        }
    }

    private fun checkNotificationsPermission() {
        when {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) -> {
                checkPhonePermission()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS) -> {
                val dialog = RequestDialog.newDialog("Notifications permission", "TrackPacer uses a background task. This allows pacing to work even when the screen is turned off. A notification will appear when pacing is in progress. However, you must allow notifications for this to work.", "REQUEST_NOTIFICATIONS_DIALOG")
                dialog.show(supportFragmentManager, "REQUEST_NOTIFICATIONS_DIALOG")
            }
            else -> requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    private fun checkPhonePermission() {
        when {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) -> {
                startService()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) -> {
                val dialog = RequestDialog.newDialog("Phone permission", "Pacing will automatically pause when there is an incoming call. However, you must allow TrackPacer to manage calls for this to work. Alternatively, always enable flight mode to ensure your training will not be disturbed!", "REQUEST_PHONE_DIALOG")
                dialog.show(supportFragmentManager, "REQUEST_PHONE_DIALOG")
            }
            else -> requestPhoneLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }
}
