package com.fig7.trackpacer

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fig7.trackpacer.data.PacingModel
import com.fig7.trackpacer.databinding.ActivityPaceBinding

class PacingActivity : AppCompatActivity() {
    private val pacingModel: PacingModel by viewModels()

    private lateinit var binding: ActivityPaceBinding
    private lateinit var waypointService: WaypointService
    private lateinit var mNM: NotificationManager

    private lateinit var mpPacingCancelled: MediaPlayer
    private lateinit var mpPacingPaused: MediaPlayer
    private lateinit var mpPacingComplete: MediaPlayer

    private val handler  = Handler(Looper.getMainLooper())
    private val runnable = Runnable { handleTimeUpdate() }
    private var broadcastReceiver = ActivityReceiver()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()

            val runDist = pacingModel.runDist
            val runLane = pacingModel.runLane
            val runTime = pacingModel.runTime
            var pacingStatus = pacingModel.pacingStatus
            if (pacingStatus == PacingStatus.ServiceStart) {
                if (waypointService.beginPacing(runDist, runLane, runTime)) {
                    pacingStatus = PacingStatus.PacingStart

                    /* stopButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.stop))
                    stopButton.isEnabled = true
                    stopButton.isClickable = true

                    nextUpLabel.text = getString(R.string.nextup, "")
                    nextUpProgress.progress = 0

                    timeToLabel.text = getString(R.string.timeto, "")
                    timeToProgress.progress = 0 */

                    handler.postDelayed(runnable, 100)
                }
            } else { // ServiceResume
                if (waypointService.resumePacing(runDist, runTime, runLane, pacingModel.pausedTime)) {
                    pacingStatus = PacingStatus.PacingResume

                    handler.postDelayed(runnable, 100)
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    private val requestNotificationsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            checkPhonePermission()
        } else {
            stopPacing(true)

            val dialog = InfoDialog.newDialog("Pacing not possible", "Notifications permission is required. Allow notifications if you wish to use TrackPacer.")
            dialog.show(supportFragmentManager, "CANNOT_FUNCTION_DIALOG")
        }
    }

    private val requestPhoneLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        startService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        binding = ActivityPaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        pacingModel.runDist = initData.getString("RunDist")!!
        pacingModel.runLaps = initData.getString("RunLaps")!!
        pacingModel.runProf = initData.getString("RunProf")!!
        pacingModel.runLane = initData.getInt("RunLane")
        pacingModel.runTime = initData.getDouble("RunTime")

        mpPacingCancelled = MediaPlayer.create(this, R.raw.cancelled)
        mpPacingCancelled.setOnCompletionListener {
            pacingModel.pacingStatus = PacingStatus.NotPacing
        }

        mpPacingComplete = MediaPlayer.create(this, R.raw.complete)
        mpPacingComplete.setOnCompletionListener {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true

            enableSpinners(true) */
        }

        mpPacingPaused = MediaPlayer.create(this, R.raw.paused)
        mpPacingPaused.setOnCompletionListener {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.resume))
            goButton.isEnabled = true
            goButton.isClickable = true

            stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop))
            stopButton.isEnabled = true
            stopButton.isClickable = true */
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            supportFragmentManager.setFragmentResultListener("REQUEST_NOTIFICATIONS_DIALOG", this) { _: String, bundle: Bundle ->
                val resultVal = bundle.getBoolean("RequestResult")
                if (resultVal) requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else stopPacing(true)
            }
        }

        supportFragmentManager.setFragmentResultListener("REQUEST_PHONE_DIALOG", this) { _: String, bundle: Bundle ->
            val resultVal = bundle.getBoolean("RequestResult")
            if (resultVal) requestPhoneLauncher.launch(Manifest.permission.READ_PHONE_STATE) else stopPacing(true)
        }

        supportFragmentManager.setFragmentResultListener("BEGIN_PACING", this) { _, _ ->
            beginPacing()
        }

        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_NOT_EXPORTED else 0
        registerReceiver(broadcastReceiver, IntentFilter("TrackPacer.PAUSE_PACING"), receiverFlags)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
    }


    private fun beginPacing() {
        pacingModel.pacingStatus = PacingStatus.CheckPermissionStart

        /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        enableSpinners(false) */

        checkNotificationsPermission()
    }

    private fun resumePacing() {
        pacingModel.pacingStatus = PacingStatus.CheckPermissionResume

        /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.resume2))
        goButton.isEnabled = false
        goButton.isClickable = false */

        checkNotificationsPermission()
    }

    private fun startService() {
        pacingModel.pacingStatus = if (pacingModel.pacingStatus == PacingStatus.CheckPermissionStart) PacingStatus.ServiceStart else PacingStatus.ServiceResume

        val intent = Intent(this, WaypointService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun pausePacing(silent: Boolean) {
        pacingModel.pacingStatus = PacingStatus.PacingPaused
        // updatePacingStatus()

        pacingModel.pausedTime = waypointService.elapsedTime()

        unbindService(connection)
        handler.removeCallbacks(runnable)

        if (silent) {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.resume))
            goButton.isEnabled   = true
            goButton.isClickable = true

            stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop))
            stopButton.isEnabled   = true
            stopButton.isClickable = true */
        } else {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause2))
            goButton.isEnabled   = false
            goButton.isClickable = false

            stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
            stopButton.isEnabled   = false
            stopButton.isClickable = false */

            mpPacingPaused.start()
        }
    }

    private fun stopPacing(silent: Boolean) {
        /* stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop2))
        stopButton.isEnabled   = false
        stopButton.isClickable = false */

        if (silent) {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled   = true
            goButton.isClickable = true

            enableSpinners(true) */
        } else {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
            goButton.isEnabled   = false
            goButton.isClickable = false */
        }

        var pacingStatus = pacingModel.pacingStatus
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

        pacingModel.pacingStatus = PacingStatus.NotPacing
        // updatePacingStatus()
    }

    private fun checkNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        } else if (!mNM.areNotificationsEnabled()) {
            stopPacing(true)

            val dialog = InfoDialog.newDialog("Pacing not possible", "Notifications permission is required. Allow notifications if you wish to use TrackPacer.")
            dialog.show(supportFragmentManager, "CANNOT_FUNCTION_DIALOG")
        } else {
            checkPhonePermission()
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

    private fun timeToString(timeInMS: Long): String {
        var timeLeft = timeInMS

        var hrs = timeLeft / 3600000L
        timeLeft -= hrs * 3600000L

        var mins = timeLeft / 60000L
        timeLeft -= mins * 60000L

        var secs = timeLeft / 1000L
        timeLeft -= secs * 1000L

        if (((hrs > 0L) || (mins > 0L)) && (timeLeft > 0L)) {
            secs += 1L
            if (secs == 60L) {
                secs = 0L
                mins += 1L
                if (mins == 60L) {
                    mins = 0L
                    hrs += 1L
                }
            }
        }

        return if (hrs > 0L) {
            val hrsStr  = String.format("%d", hrs)
            val minsStr = String.format("%02d", mins)
            val secsStr = String.format("%02d", secs)
            getString(R.string.base_time_hms, hrsStr, minsStr, secsStr)
        } else if (mins > 0L) {
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

        var pacingStatus = pacingModel.pacingStatus
        if (elapsedTime < 0) {
            elapsedSgn = "-"
            elapsedTime = -elapsedTime
        } else if ((pacingStatus == PacingStatus.PacingStart) || (pacingStatus == PacingStatus.PacingResume)) {
            pacingStatus = PacingStatus.Pacing
            // updatePacingStatus()

            waypointService.beginRun(elapsedTime)

            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.pause))
            goButton.isClickable = true
            goButton.isEnabled = true */
        } else {
            /* nextUpLabel.text = getString(R.string.nextup, waypointService.waypointName())
            nextUpProgress.progress = (100.0*waypointService.waypointProgress(elapsedTime)).toInt() */

            var remainingSgn = ""
            var remainingTime = waypointService.timeRemaining(elapsedTime)
            // timeToProgress.progress = min(100, (100.0 - 100.0*(remainingTime / runTimeFromSpinner())).toInt())

            if (remainingTime < 0) {
                remainingSgn = "-"
                remainingTime = -remainingTime
            }

            val remainingStr = timeToString(remainingTime)
            // timeToLabel.text = getString(R.string.timeto, getString(R.string.signed_time, remainingSgn, remainingStr))
        }

        val elapsedStr = timeToFullString(elapsedTime)
        // timerView.text = getString(R.string.signed_time, elapsedSgn, elapsedStr)

        handler.postDelayed(runnable, 100)
    }

    fun handleIncomingCall() {
        when (pacingModel.pacingStatus) {
            PacingStatus.NotPacing ->    return
            PacingStatus.PacingPaused -> return
            PacingStatus.Pacing ->       return // pausePacing(true)
            else ->                      return // stopPacing(true)
        }
    }
}