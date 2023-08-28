package com.fig7.trackpacer

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fig7.trackpacer.databinding.ActivityMainBinding

// States
// 1. Initial:   NotPacing                          Stop: Disabled Play:  Enabled

// 2a. Play  ->  NotPacing    -> PacingStart        Stop: Enabled  Play:  Disabled       Count up from -5000L + 3, 2, 1, Go!
// 2b.           PacingStart  -> Pacing             Stop: Enabled  Pause: Enabled        Elapsed time >= 0

// 3.  Stop1  -> PacingStart  -> NotPacing          Stop: Disabled Play:  Disabled       Pacing cancelled -> Stop: Disabled Play:  Enabled

// 4.  Stop2  -> Pacing       -> NotPacing          Stop: Disabled Play:  Disabled       Pacing complete  -> Stop: Disabled Play:  Enabled

// 5a. Pause  -> Pacing       -> PacingPaused       Stop: Disabled Play:  Disabled       Pacing paused    -> Stop: Enabled  Play:  Enabled
// 5b. Resume -> PacingPaused -> Pacing             Stop: Disabled Play:  Disabled       Pacing resumed   -> Stop: Enabled  Pause: Enabled


// Ship!
// How about "Wait for screen lock before starting (i.e. start the service, but wait for screen off). Then auto-stop on screen unlock. Yeah.

// Fix privacy policy on mobile
// Add 1K, and update start line image + feature one.
// Error handling on start / convert to jetpack / code review / ship!
// Clip recording / replacement / Tabs
// Auto enable flight mode. Auto Lock Screen. Auto unlock screen?
// Auto stop using voice recognition, GPS (!?) or detect power button press?
// Add history + set own times (edit times, and edit distances). With Runpacer: could do set a point on a map and set the time (use GPS, eek!).
// Profiles, clips, settings, history for next version. Run, history, clips, settings tabs
// Share/submit routes + Auto airplane mode setting too
// 3K/5K setting for start/finish position
// Add support for 200m indoor running tracks?
// Countdown TTS sound boards

// Updated UI.
// Remove Clock + Next up / Time to target components. Add "On your marks...".
// Then, add Clock, + Next up / Time to target. Remove Distance/lane/time/pace. Add Distance run (when on pace): 407.66m
// Add Stop / Power start (use power icon!). So, it's either a play button or power button (non-functional)
// Start & Finish: Graphic with short green line for start, red line for finish. Add numbers, so it's obvious.
// In center, you can put 1 lap. 2 laps. 3 laps, 3 3/4 laps (First lap is 300m, Last lap is lap 4), 4 laps (+9.34m),
// 5 laps, 7 1/2 laps (First lap is 200m, or Last lap is 200m, Last lap is lap 8), 12 1/2 laps (First / Last is, Last lap is lap 13)
// up to 25 laps!

// On running screen, lose bottom navigation.

// On running screen, perhaps have:
//             Clock
//
// Distance:
//   10000m in L2 (10123.00m)
//
// Expected time:
//   30:25.27 (5:30.21/km)
//   (30:00.00 when in L1)   // Only show if not in L1!
//
// Profile:
//   Fixed pace

// Distance run (when on pace):
//   5325.23m
//
// Next up: Waypoint (Distance)
//
// Time to target:
//
// Stop                     Go

// Then have a completion screen. Really just change Next up / Time to target to
// Pacing complete and saved! You finished 1.567s early or 6.432s late
// Maybe auto cancel if time is less than 30s? Yes.
// And buttons to delete (We auto save the workout) and Home. Or just add delete button. Can play again if you want.
// Perhaps just add another button row. Delete and home. Trash can + home:

// Change stop to Home, when stopped. Yesh. Just do deleting from within history.

class MainActivity : AppCompatActivity() {
    lateinit var dataManager: DataManager
    var pacingStatus = PacingStatus.NotPacing

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNM: NotificationManager
    private lateinit var waypointService: WaypointService

    private var runDistance: String = ""
    private var runLane: Int        = -1
    private var runTime: Double     = -1.0
    private var pausedTime: Long    = -1L

    private lateinit var mpPacingCancelled: MediaPlayer
    private lateinit var mpPacingPaused: MediaPlayer
    private lateinit var mpPacingComplete: MediaPlayer

    private var broadcastReceiver = ActivityReceiver()

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
        startService()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WaypointService.LocalBinder
            waypointService = binder.getService()

            if (pacingStatus == PacingStatus.ServiceStart) {
                if (waypointService.beginPacing(runDistance, runTime, runLane)) {
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
                if (waypointService.resumePacing(runDistance, runTime, runLane, pausedTime)) {
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView       = binding.navView
        val navController = binding.mainView.getFragment<NavHostFragment>().navController
        navView.setupWithNavController(navController)

        savedInstanceState?.run {
            pacingStatus = PacingStatus.values()[getInt("PACING_STATUS")]
            pausedTime = getLong("PAUSED_TIME")
        }

        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        dataManager = DataManager(filesDir)
        try {
            dataManager.initDistances(resources.getStringArray(R.array.distance_array))
        } catch (_: Exception) {
            val dialog = DataErrorDialog.newDialog("initializing", true)
            dialog.show(supportFragmentManager, "DATA_ERROR_DIALOG")
        }

        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_NOT_EXPORTED else 0
        registerReceiver(broadcastReceiver, IntentFilter("TrackPacer.PAUSE_PACING"), receiverFlags )

        supportFragmentManager.setFragmentResultListener("BEGIN_PACING", this) { _, bundle ->
            runDistance = bundle.getString("RUN_DISTANCE")!!
            runLane     = bundle.getInt("RUN_LANE")
            runTime     = bundle.getDouble("RUN_TIME")

            binding.navView.visibility = View.GONE;
            beginPacing()
        }

        /* spinnerDistance = findViewById(R.id.spinner_distance)
        val spinnerDistanceAdapter = ArrayAdapter(this, R.layout.spinner_item, dataManager.distanceArray)
        spinnerDistanceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerDistance.adapter = spinnerDistanceAdapter

        savedInstanceState?.run { val spinnerDistancePos = getInt("SP_DISTANCE"); spinnerDistance.setSelection(spinnerDistancePos)  }
        spinnerDistance.onItemSelectedListener = this

        spinnerLane = findViewById(R.id.spinner_lane)
        val laneArray: Array<String> = resources.getStringArray(R.array.lane_array)
        val spinnerLaneAdapter = ArrayAdapter(this, R.layout.spinner_item, laneArray)
        spinnerLaneAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerLane.adapter = spinnerLaneAdapter
        savedInstanceState?.run { val spinnerLanePos = getInt("SP_LANE"); spinnerLane.setSelection(spinnerLanePos)  }

        spinnerTime = findViewById(R.id.spinner_time)
        val spinnerTimeAdapter = ArrayAdapter(this, R.layout.spinner_item, dataManager.timeMap[spinnerDistance.selectedItem.toString()]!!)
        spinnerTimeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTime.adapter = spinnerTimeAdapter
        // *? Test this, it didn't appear to work! savedInstanceState?.run { spinnerTime.setSelection(getInt("SP_TIME")) }
        // Maybe need to move spinnerDistance.onItemSelectedListener = this to onResume()? Or something!

        spinnerProfile = findViewById(R.id.spinner_profile)
        val profileArray: Array<String> = resources.getStringArray(R.array.profile_array)
        val spinnerProfileAdapter = ArrayAdapter(this, R.layout.spinner_item, profileArray)
        spinnerProfileAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerProfile.adapter = spinnerProfileAdapter
        savedInstanceState?.run { spinnerTime.setSelection(getInt("SP_PROFILE")) }

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

        editButton = findViewById(R.id.button_time)
        editButton.setOnClickListener {
            val dialog = EditTimeDialog.newDialog(spinnerTime.selectedItem.toString(), dataManager.timeMap[spinnerDistance.selectedItem.toString()]!!, "EDIT_TIME_DIALOG")
            dialog.show(supportFragmentManager, "EDIT_TIME_DIALOG")
        }

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
                goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.resume))
                goButton.isEnabled = true
                goButton.isClickable = true

                stopButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop))
                stopButton.isEnabled = true
                stopButton.isClickable = true

                enableSpinners(false)
            }
            else -> throw IllegalStateException()
        } */

        mpPacingCancelled = MediaPlayer.create(this, R.raw.cancelled)
        mpPacingCancelled.setOnCompletionListener {
            /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
            goButton.isEnabled = true
            goButton.isClickable = true

            enableSpinners(true) */
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

        supportFragmentManager.setFragmentResultListener("EDIT_TIME_DIALOG", this) { _: String, bundle: Bundle ->
            try {
                val runDistance = "400m" // spinnerDistance.selectedItem.toString()
                when (EditResult.values()[bundle.getInt("EditResult")]) {
                    EditResult.Delete -> {
                        val newIndex = dataManager.deleteTime(runDistance, bundle.getString("EditTime"))
                        // updateTimeSpinner(runDistance, newIndex)
                    }

                    EditResult.Add -> {
                        val newIndex = dataManager.addTime(runDistance, bundle.getString("EditTime"))
                        // updateTimeSpinner(runDistance, newIndex)
                    }

                    EditResult.Set -> {
                        val newIndex = dataManager.replaceTime(runDistance, bundle.getString("OrigTime"), bundle.getString("EditTime"))
                        // updateTimeSpinner(runDistance, newIndex)
                    }

                    EditResult.Cancel -> {}
                }
            } catch (_: Exception) {
                val dialog = DataErrorDialog.newDialog("updating", false)
                dialog.show(supportFragmentManager, "DATA_ERROR_DIALOG")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // updatePacingStatus()

        val phoneIcon = findViewById<ImageView>(R.id.phone_status)
        val phonePermission = (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(this, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = findViewById<TextView>(R.id.delay_setting)
        delaySetting.setText(R.string.start_delay)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putInt("PACING_STATUS", pacingStatus.ordinal)
            putLong("PAUSED_TIME", pausedTime)

            /* putInt("SP_DISTANCE", spinnerDistance.selectedItemPosition)
            putInt("SP_LANE",     spinnerLane.selectedItemPosition)
            putInt("SP_TIME",     spinnerTime.selectedItemPosition)
            putInt("SP_PROFILE",  spinnerProfile.selectedItemPosition)

            putString("TIMER_VAL", timerView.text.toString())

            putString("NEXTUP_LABEL", nextUpLabel.text.toString())
            putInt("NEXTUP_PROGRESS", nextUpProgress.progress)

            putString("TIMETO_LABEL", timeToLabel.text.toString())
            putInt("TIMETO_PROGRESS", timeToProgress.progress) */
        }

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
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

    private fun beginPacing() {
        pacingStatus = PacingStatus.CheckPermissionStart

        /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play2))
        goButton.isEnabled = false
        goButton.isClickable = false

        enableSpinners(false) */

        checkNotificationsPermission()
    }

    private fun resumePacing() {
        pacingStatus = PacingStatus.CheckPermissionResume

        /* goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.resume2))
        goButton.isEnabled = false
        goButton.isClickable = false */

        checkNotificationsPermission()
    }

    private fun startService() {
        pacingStatus = if (pacingStatus == PacingStatus.CheckPermissionStart) PacingStatus.ServiceStart else PacingStatus.ServiceResume

        val intent = Intent(this, WaypointService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun pausePacing(silent: Boolean) {
        pacingStatus = PacingStatus.PacingPaused
        // updatePacingStatus()

        pausedTime = waypointService.elapsedTime()

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
        // updatePacingStatus()
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
}
