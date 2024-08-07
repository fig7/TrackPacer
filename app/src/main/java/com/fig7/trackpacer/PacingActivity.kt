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
import android.view.WindowManager
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
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.ActivityPaceBinding
import com.fig7.trackpacer.enums.PacingStatus
import com.fig7.trackpacer.receiver.PacingReceiver
import com.fig7.trackpacer.receiver.ScreenReceiver
import com.fig7.trackpacer.util.Bool
import com.fig7.trackpacer.util.timeToAlmostFullString
import com.fig7.trackpacer.util.timeToString
import com.fig7.trackpacer.waypoint.WaypointService
import com.fig7.trackpacer.waypoint.distanceFor
import com.fig7.trackpacer.waypoint.timeFor
import java.util.Locale

class PacingActivity: AppCompatActivity() {
    private val pacingModel: PacingModel by viewModels()
    private val statusModel: StatusModel by viewModels()
    private val resultModel: ResultModel by viewModels()

    private lateinit var binding: ActivityPaceBinding
    private lateinit var waypointService: WaypointService
    private lateinit var mNM: NotificationManager

    private lateinit var mpPacingCancelled: MediaPlayer
    private lateinit var mpPacingPaused: MediaPlayer
    private lateinit var mpPacingComplete: MediaPlayer

    private val handler = Handler(Looper.getMainLooper())
    private val pacingRunnable = Runnable { handleTimeUpdate() }

    private var screenReceiver    = ScreenReceiver()
    private var broadcastReceiver = PacingReceiver()

    private lateinit var serviceIntent: Intent
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()

            val runDist = pacingModel.runDist
            val runLane = pacingModel.runLane
            val runTime = pacingModel.runTime
            val alternateStart = pacingModel.alternateStart

            val pacingStatus = statusModel.pacingStatus.value
            if(pacingStatus == PacingStatus.ServiceStart) {
                waypointService.beginPacing(runDist, runLane, runTime, alternateStart)
                if(statusModel.powerStart) {
                    // Power start
                    statusModel.setPacingStatus(PacingStatus.PacingWait)

                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    startScreenReceiver()
                } else {
                    // Delay start
                    statusModel.setPacingStatus(PacingStatus.PacingStart)

                    if(waypointService.delayStart((statusModel.startDelay.toDouble() * 1000.0).toLong(), statusModel.quickStart)) {
                        handler.postDelayed(pacingRunnable, 100)
                    } else {
                        stopPacing(true)
                    }
                }
            } else if(pacingStatus == PacingStatus.ServiceResume) {
                statusModel.setPacingStatus(PacingStatus.PacingResume)

                if(statusModel.powerStart) {
                    startScreenReceiver()
                }

                if(waypointService.resumePacing(runDist, runLane, runTime, alternateStart, pacingModel.elapsedTimeL)) {
                    handler.postDelayed(pacingRunnable, 100)
                } else {
                    stopPacing(true)
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

    private fun startScreenReceiver() {
        val screenAction = IntentFilter(Intent.ACTION_SCREEN_OFF)
        screenAction.addAction(Intent.ACTION_SCREEN_ON)

        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_NOT_EXPORTED else 0
        registerReceiver(screenReceiver, screenAction, receiverFlags)
    }

    private fun isPacing(pacingStatus: PacingStatus? = statusModel.pacingStatus.value): Bool {
        return ((pacingStatus == PacingStatus.PacingStart) || (pacingStatus == PacingStatus.PacingResume) ||
                (pacingStatus == PacingStatus.PacingWait)  || (pacingStatus == PacingStatus.Pacing))
    }

    private fun isWaiting(pacingStatus: PacingStatus? = statusModel.pacingStatus.value): Bool {
        return (pacingStatus == PacingStatus.PacingWait)
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
        pacingModel.alternateStart = initData.getBoolean("AlternateStart")

        statusModel.startDelay     = initData.getString("StartDelay")!!
        statusModel.powerStart     = initData.getBoolean("PowerStart")
        statusModel.quickStart     = initData.getBoolean("QuickStart")

        pacingModel.totalDist = distanceFor(pacingModel.runDist, pacingModel.runLane)
        pacingModel.totalDistStr =
            if(pacingModel.runDist == "1 mile") {
                if(pacingModel.runLane == 1) pacingModel.runDist else String.format(Locale.ROOT, "%.2f miles", pacingModel.totalDist/1609.34)
            } else {
                if (pacingModel.runLane == 1) String.format(Locale.ROOT, "%dm", pacingModel.totalDist.toInt()) else String.format(Locale.ROOT, "%.2fm", pacingModel.totalDist)
            }

        val totalTime = timeFor(pacingModel.runDist, pacingModel.runLane, pacingModel.runTime)
        pacingModel.totalTimeStr = timeToAlmostFullString(resources, totalTime.toLong())

        val totalPace = (1000.0 * totalTime) / pacingModel.totalDist
        pacingModel.totalPaceStr = timeToString(resources, totalPace.toLong())

        mpPacingCancelled = MediaPlayer.create(this, R.raw.cancelled)
        mpPacingCancelled.setOnCompletionListener {
            statusModel.setPacingStatus(PacingStatus.NotPacing)
        }

        mpPacingComplete = MediaPlayer.create(this, R.raw.complete)
        mpPacingComplete.setOnCompletionListener {
            statusModel.setPacingStatus(PacingStatus.NotPacing)

            val resultBundle = Bundle()
            resultBundle.putString("StartDelay", statusModel.startDelay)
            resultBundle.putBoolean("PowerStart", statusModel.powerStart)
            resultBundle.putBoolean("QuickStart", statusModel.quickStart)
            resultBundle.putParcelable("resultParcel", resultModel.resultData)

            val intent = Intent(this, CompletionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            intent.putExtras(resultBundle)

            startActivity(intent)
            finish()
        }

        mpPacingPaused = MediaPlayer.create(this, R.raw.paused)
        mpPacingPaused.setOnCompletionListener {
            statusModel.setPacingStatus(PacingStatus.PacingPaused)
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

        val broadcastAction = IntentFilter("TrackPacer.CALL_PAUSE_PACING")
        broadcastAction.addAction("TrackPacer.POWER_PAUSE_PACING")
        broadcastAction.addAction("TrackPacer.POWER_BEGIN_PACING")

        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_NOT_EXPORTED else 0
        registerReceiver(broadcastReceiver, broadcastAction, receiverFlags)

        // Ignore back button presses (if we are pacing or about to start pacing)
        val backPressedCallback = onBackPressedDispatcher.addCallback(this) { }
        statusModel.pacingStatus.observe(this) { pacingStatus ->
            backPressedCallback.isEnabled = (pacingStatus != PacingStatus.NotPacing)
        }
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(pacingRunnable)
    }

    override fun onResume() {
        super.onResume()

        if(isPacing() && !isWaiting()) {
            handler.post(pacingRunnable)
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
        statusModel.setPacingStatus(PacingStatus.CheckPermissionStart)
        checkNotificationsPermission()
    }

    private fun resumePacing() {
        statusModel.setPacingStatus(PacingStatus.CheckPermissionResume)
        checkNotificationsPermission()
    }

    private fun startService() {
        val pacingStatus = statusModel.pacingStatus.value
        statusModel.setPacingStatus(if (pacingStatus == PacingStatus.CheckPermissionStart) PacingStatus.ServiceStart else PacingStatus.ServiceResume)

        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        handler.removeCallbacks(pacingRunnable)
        unbindService(serviceConnection)
    }

    private fun stopService() {
        if(statusModel.powerStart) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            unregisterReceiver(screenReceiver)
        }

        handler.removeCallbacks(pacingRunnable)
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }

    private fun pausePacing(silent: Bool) {
        // Record the pacing progress
        val elapsedTime = waypointService.elapsedTime()
        pacingModel.setElapsedTime(elapsedTime)

        val distRun = waypointService.distOnPace(elapsedTime)
        pacingModel.setDistRun(distRun)

        val name          = waypointService.waypointName()
        val progress      = waypointService.waypointProgress(elapsedTime)
        val remainingTime = waypointService.timeRemaining(elapsedTime)
        pacingModel.setWaypointProgress(name, progress, remainingTime)

        // Stop the service
        stopService()

        // Update the status
        if(silent) {
            statusModel.setPacingStatus(PacingStatus.PacingPaused)
        } else {
            statusModel.setPacingStatus(PacingStatus.PacingPause)
            mpPacingPaused.start()
        }
    }

    private fun stopPacing(silent: Bool) {
        val pacingStatus = statusModel.pacingStatus.value
        if(isPacing(pacingStatus)) {
            stopService()

            if(silent) {
                statusModel.setPacingStatus(PacingStatus.NotPacing)
            } else if ((pacingStatus == PacingStatus.Pacing) && (pacingModel.elapsedTimeL >= 40000L)) {
                resultModel.setPacingResult(resources, pacingModel)
                statusModel.setPacingStatus(PacingStatus.PacingComplete)
                mpPacingComplete.start()
            } else {
                statusModel.setPacingStatus(PacingStatus.PacingCancel)
                mpPacingCancelled.start()
            }
        } else if((pacingStatus == PacingStatus.PacingPaused) && !silent) {
            if(pacingModel.elapsedTimeL >= 40000L) {
                resultModel.setPacingResult(resources, pacingModel)
                statusModel.setPacingStatus(PacingStatus.PacingComplete)
                mpPacingComplete.start()
            } else {
                statusModel.setPacingStatus(PacingStatus.PacingCancel)
                mpPacingCancelled.start()
            }
        } else {
            statusModel.setPacingStatus(PacingStatus.NotPacing)
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
        if(elapsedTime >= 0L) {
            val pacingStatus = statusModel.pacingStatus.value
            if((pacingStatus == PacingStatus.PacingStart) || (pacingStatus == PacingStatus.PacingResume)) {
                statusModel.setPacingStatus(PacingStatus.Pacing)
                if(pacingStatus == PacingStatus.PacingStart) { resultModel.initPacingResult(System.currentTimeMillis(), pacingModel) }
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

    private fun powerStart() {
        statusModel.setPacingStatus(PacingStatus.PacingStart)
        if(waypointService.powerStart(statusModel.quickStart)) {
            handler.postDelayed(pacingRunnable, 100)
        } else {
            stopPacing(true)
        }
    }

    fun handleIncomingIntent(begin: Bool, silent: Bool) {
        val pacingStatus = statusModel.pacingStatus.value
        if(begin) {
            if(pacingStatus != PacingStatus.PacingWait) { return }
            powerStart()
        } else {
            when(pacingStatus) {
                PacingStatus.NotPacing    -> return
                PacingStatus.PacingPaused -> return
                PacingStatus.Pacing       -> pausePacing(silent)
                else                      -> stopPacing(silent)
            }
        }
    }
}