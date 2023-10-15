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
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fig7.trackpacer.dialog.InfoDialog
import com.fig7.trackpacer.dialog.RequestDialog
import com.fig7.trackpacer.data.PacingModel
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.databinding.ActivityPaceBinding
import com.fig7.trackpacer.enums.PacingStatus
import com.fig7.trackpacer.receiver.ActivityReceiver
import com.fig7.trackpacer.util.timeToAlmostFullString
import com.fig7.trackpacer.util.timeToString
import com.fig7.trackpacer.waypoint.WaypointService
import com.fig7.trackpacer.waypoint.distanceFor
import com.fig7.trackpacer.waypoint.timeFor


class PacingActivity: AppCompatActivity() {
    private val pacingModel: PacingModel by viewModels()
    private val resultModel: ResultModel by viewModels()

    private lateinit var binding: ActivityPaceBinding
    private lateinit var waypointService: WaypointService
    private lateinit var mNM: NotificationManager

    private lateinit var mpPacingCancelled: MediaPlayer
    private lateinit var mpPacingPaused: MediaPlayer
    private lateinit var mpPacingComplete: MediaPlayer

    private val handler  = Handler(Looper.getMainLooper())
    private val pacingRunnable = Runnable { handleTimeUpdate() }
    private var broadcastReceiver = ActivityReceiver()

    private lateinit var serviceIntent: Intent
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()
            pacingModel.setServiceReady(true)

            val runDist = pacingModel.runDist
            val runLane = pacingModel.runLane
            val runTime = pacingModel.runTime
            val pacingStatus = pacingModel.pacingStatus.value
            if (pacingStatus == PacingStatus.ServiceStart) {
                if (waypointService.beginPacing(runDist, runLane, runTime)) {
                    pacingModel.setPacingStatus(PacingStatus.PacingStart)
                }
            } else if(pacingStatus == PacingStatus.ServiceResume) {
                if (waypointService.resumePacing(runDist, runTime, runLane, pacingModel.pausedTime)) {
                    pacingModel.setPacingStatus(PacingStatus.PacingResume)
                }
            }

            handler.postDelayed(pacingRunnable, 100)
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

    private fun isPacing(pacingStatus: PacingStatus? = pacingModel.pacingStatus.value): Boolean {
        return ((pacingStatus == PacingStatus.Pacing) || (pacingStatus == PacingStatus.PacingStart) || (pacingStatus == PacingStatus.PacingResume))
    }

    private fun serviceReady(): Boolean {
        return (pacingModel.serviceReady.value ?: false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        serviceIntent = Intent(this, WaypointService::class.java)

        if(isPacing()) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        binding = ActivityPaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        pacingModel.runDist = initData.getString("RunDist")!!
        pacingModel.runLaps = initData.getString("RunLaps")!!
        pacingModel.runProf = initData.getString("RunProf")!!
        pacingModel.runLane = initData.getInt("RunLane")
        pacingModel.runTime = initData.getDouble("RunTime")

        pacingModel.totalDist = distanceFor(pacingModel.runDist, pacingModel.runLane)
        pacingModel.totalDistStr =
            if(pacingModel.runDist == "1 mile") {
                if(pacingModel.runLane == 1) pacingModel.runDist else String.format("%.2f miles", pacingModel.totalDist/1609.34)
            } else {
                if (pacingModel.runLane == 1) String.format("%dm", pacingModel.totalDist.toInt()) else String.format("%.2fm", pacingModel.totalDist)
            }

        val totalTime = timeFor(pacingModel.runDist, pacingModel.runLane, pacingModel.runTime)
        pacingModel.totalTimeStr = timeToAlmostFullString(resources, totalTime.toLong())

        val totalPace = (1000.0 * totalTime) / pacingModel.totalDist
        pacingModel.totalPaceStr = timeToString(resources, totalPace.toLong())

        mpPacingCancelled = MediaPlayer.create(this, R.raw.cancelled)
        mpPacingCancelled.setOnCompletionListener {
            pacingModel.setPacingStatus(PacingStatus.NotPacing)
        }

        mpPacingComplete = MediaPlayer.create(this, R.raw.complete)
        mpPacingComplete.setOnCompletionListener {
            pacingModel.setPacingStatus(PacingStatus.NotPacing)

            val resultBundle = Bundle()
            resultBundle.putParcelable("resultParcel", resultModel.resultData)

            val intent = Intent(this, CompletionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            intent.putExtras(resultBundle)

            startActivity(intent)
            finish()
        }

        mpPacingPaused = MediaPlayer.create(this, R.raw.paused)
        mpPacingPaused.setOnCompletionListener {
            pacingModel.setPacingStatus(PacingStatus.PacingPaused)
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

        supportFragmentManager.setFragmentResultListener("PAUSE_PACING", this) { _, _ ->
            pausePacing(false)
        }

        supportFragmentManager.setFragmentResultListener("RESUME_PACING", this) { _, _ ->
            resumePacing()
        }

        supportFragmentManager.setFragmentResultListener("STOP_PACING", this) { _, _ ->
            stopPacing(false)
        }

        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_NOT_EXPORTED else 0
        registerReceiver(broadcastReceiver, IntentFilter("TrackPacer.PAUSE_PACING"), receiverFlags)

        // Ignore back button presses (if we are pacing or about to start pacing)
        val backPressedCallback = onBackPressedDispatcher.addCallback(this) { }
        pacingModel.pacingStatus.observe(this) { pacingStatus ->
            backPressedCallback.isEnabled = (pacingStatus != PacingStatus.NotPacing)
        }
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(pacingRunnable)
    }

    override fun onResume() {
        super.onResume()

        if(isPacing() && serviceReady()) {
            handleTimeUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val pacing = isPacing()
        if (pacing && isFinishing) {
            stopService()
        } else if (pacing) {
            unbindService()
        }

        unregisterReceiver(broadcastReceiver)
    }

    private fun beginPacing() {
        pacingModel.setPacingStatus(PacingStatus.CheckPermissionStart)
        checkNotificationsPermission()
    }

    private fun resumePacing() {
        pacingModel.setPacingStatus(PacingStatus.CheckPermissionResume)
        checkNotificationsPermission()
    }

    private fun startService() {
        val pacingStatus = pacingModel.pacingStatus.value
        pacingModel.setPacingStatus(if (pacingStatus == PacingStatus.CheckPermissionStart) PacingStatus.ServiceStart else PacingStatus.ServiceResume)

        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        handler.removeCallbacks(pacingRunnable)
        unbindService(serviceConnection)

        pacingModel.setServiceReady(false)
    }

    private fun stopService() {
        handler.removeCallbacks(pacingRunnable)
        unbindService(serviceConnection)
        stopService(serviceIntent)

        pacingModel.setServiceReady(false)
    }

    private fun pausePacing(silent: Boolean) {
        pacingModel.pausedTime = waypointService.elapsedTime()
        stopService()

        if (silent) {
            pacingModel.setPacingStatus(PacingStatus.PacingPaused)
        } else {
            pacingModel.setPacingStatus(PacingStatus.PacingPause)
            mpPacingPaused.start()
        }
    }

    private fun stopPacing(silent: Boolean) {
        val pacingStatus = pacingModel.pacingStatus.value
        if (isPacing(pacingStatus)) {
            stopService()

            if (silent) {
                pacingModel.setPacingStatus(PacingStatus.NotPacing)
            } else if ((pacingStatus == PacingStatus.Pacing) && (pacingModel.elapsedTimeL >= 40000L)) {
                resultModel.setPacingResult(resources, pacingModel)
                pacingModel.setPacingStatus(PacingStatus.PacingComplete)
                mpPacingComplete.start()
            } else {
                pacingModel.setPacingStatus(PacingStatus.PacingCancel)
                mpPacingCancelled.start()
            }
        } else if ((pacingStatus == PacingStatus.PacingPaused) && !silent) {
            if (pacingModel.elapsedTimeL >= 40000L) {
                resultModel.setPacingResult(resources, pacingModel)
                pacingModel.setPacingStatus(PacingStatus.PacingComplete)
                mpPacingComplete.start()
            } else {
                pacingModel.setPacingStatus(PacingStatus.PacingCancel)
                mpPacingCancelled.start()
            }
        } else {
            pacingModel.setPacingStatus(PacingStatus.NotPacing)
        }
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

    private fun handleTimeUpdate() {
        val elapsedTime = waypointService.elapsedTime()
        if (elapsedTime >= 0L) {
            val pacingStatus = pacingModel.pacingStatus.value
            if ((pacingStatus == PacingStatus.PacingStart) || (pacingStatus == PacingStatus.PacingResume)) {
                pacingModel.setPacingStatus(PacingStatus.Pacing)
                resultModel.initPacingResult(System.currentTimeMillis(), pacingModel)
            } else {
                val distRun = waypointService.distOnPace(elapsedTime)
                pacingModel.setDistRun(distRun)

                val name          = waypointService.waypointName()
                val progress      = waypointService.waypointProgress(elapsedTime)
                val remainingTime = waypointService.timeRemaining(elapsedTime)
                pacingModel.setWaypointProgress(name, progress, remainingTime)
            }
        }

        pacingModel.setElapsedTime(elapsedTime)
        handler.postDelayed(pacingRunnable, 100)
    }

    fun handleIncomingCall() {
        when (pacingModel.pacingStatus.value) {
            PacingStatus.NotPacing ->    return
            PacingStatus.PacingPaused -> return
            PacingStatus.Pacing ->       pausePacing(true)
            else ->                      stopPacing(true)
        }
    }
}