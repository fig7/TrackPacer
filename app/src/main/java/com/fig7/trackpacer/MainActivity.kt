package com.fig7.trackpacer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fig7.trackpacer.dialog.StorageErrorDialog
import com.fig7.trackpacer.data.HistoryModel
import com.fig7.trackpacer.data.StorageModel
import com.fig7.trackpacer.databinding.ActivityMainBinding
import com.fig7.trackpacer.dialog.HistoryErrorDialog
import com.fig7.trackpacer.enums.EditResult
import com.fig7.trackpacer.ui.run.RunViewModel

// * Please update for new states
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
// Add a feature to just enter the number of laps (Handy if somebody wants to do reps)

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
// Or just done?

const val tpVersion = "1.3"

class MainActivity: AppCompatActivity() {
    private val storageModel: StorageModel by viewModels()
    private val historyModel: HistoryModel by viewModels()

    private val runViewModel: RunViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!storageModel.storageDataOK) {
            val dialog = StorageErrorDialog.newDialog("initializing", true)
            dialog.show(supportFragmentManager, "DATA_ERROR_DIALOG")
        }

        if(!historyModel.historyDataOK) {
            val dialog = HistoryErrorDialog.newDialog("initializing", true)
            dialog.show(supportFragmentManager, "HISTORY_ERROR_DIALOG")
        }

        historyModel.loadHistory()
        if(!historyModel.historyDataOK) {
            val dialog = HistoryErrorDialog.newDialog("loading", true)
            dialog.show(supportFragmentManager, "HISTORY_ERROR_DIALOG")
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView       = binding.navView
        val navController = binding.mainView.getFragment<NavHostFragment>().navController
        navView.setupWithNavController(navController)

        supportFragmentManager.setFragmentResultListener("EDIT_TIME", this) { _: String, bundle: Bundle ->
            try {
                val storageManager = storageModel.storageManager
                val runDist = bundle.getString("EditDist")!!
                when (EditResult.values()[bundle.getInt("EditResult")]) {
                    EditResult.Delete -> {
                        val newIndex = storageManager.deleteTime(runDist, bundle.getString("EditTime"))
                        runViewModel.selectTime(newIndex)
                    }

                    EditResult.Add -> {
                        val newIndex = storageManager.addTime(runDist, bundle.getString("EditTime"))
                        runViewModel.selectTime(newIndex)
                    }

                    EditResult.Set -> {
                        val newIndex = storageManager.replaceTime(runDist, bundle.getString("OrigTime"), bundle.getString("EditTime"))
                        runViewModel.selectTime(newIndex)
                    }

                    EditResult.Cancel -> {}
                }
            } catch (_: Exception) {
                val dialog = StorageErrorDialog.newDialog("updating", false)
                dialog.show(supportFragmentManager, "DATA_ERROR_DIALOG")
            }
        }
    }
}
