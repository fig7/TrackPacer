package com.fig7.trackpacer.ui.pace

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.PacingActivity
import com.fig7.trackpacer.enums.PacingStatus
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.PacingModel
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.FragmentPaceBinding
import com.fig7.trackpacer.util.Int64
import com.fig7.trackpacer.util.timeToFullString
import com.fig7.trackpacer.util.timeToString
import java.lang.Integer.min

class PaceFragment: Fragment() {
    private var binding: FragmentPaceBinding? = null

    private lateinit var afm: FragmentManager
    private val pacingModel: PacingModel by activityViewModels()
    private val statusModel: StatusModel by activityViewModels()

    private lateinit var distRun: TextView
    private lateinit var nextUpLabel: TextView
    private lateinit var nextUpProgress: ProgressBar

    private lateinit var timeToLabel: TextView
    private lateinit var timeToProgress: ProgressBar

    private lateinit var setButton: Button
    private lateinit var goButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var pacingIcon: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPaceBinding.inflate(inflater, container, false)
        val paceView = binding!!

        val pacingActivity = activity as PacingActivity
        afm = pacingActivity.supportFragmentManager

        val timerView = paceView.timeView
        timerView.text = timeToFullString(resources, 0L)
        pacingModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime: Int64 ->
            timerView.text = timeToFullString(resources, elapsedTime)
        }

        val runDistLabel = paceView.labelPacingDist
        runDistLabel.text = getString(R.string.label_distance2, pacingModel.runLane)

        val runDistView = paceView.pacingDist
        runDistView.text = getString(R.string.pace_dist, pacingModel.totalDistStr, pacingModel.runLaps)

        val runTimeView = paceView.pacingTime
        runTimeView.text = getString(R.string.pace_pace, pacingModel.totalTimeStr, pacingModel.totalPaceStr)

        val runProfView = paceView.pacingProf
        runProfView.text = pacingModel.runProf

        distRun = paceView.distRun
        distRun.text = ""

        nextUpLabel = paceView.nextupLabel
        nextUpLabel.text = getString(R.string.nextup, "")

        pacingModel.waypointName.observe(viewLifecycleOwner) { waypointName: String ->
            nextUpLabel.text = getString(R.string.nextup, waypointName)
        }

        nextUpProgress = paceView.nextupProgress
        nextUpProgress.progress = 0

        pacingModel.waypointProgress.observe(viewLifecycleOwner) { progress: Double ->
            nextUpProgress.progress = (100.0*progress).toInt()
        }

        timeToLabel = paceView.timetoLabel
        timeToLabel.text = getString(R.string.timeto, "")

        timeToProgress = paceView.timetoProgress
        timeToProgress.progress = 0

        pacingModel.timeRemaining.observe(viewLifecycleOwner) { remainingTime: Int64? ->
            if(remainingTime == null) {
                distRun.text = ""

                timeToLabel.text = getString(R.string.timeto, "")
                timeToProgress.progress = 0
            } else {
                distRun.text = getString(R.string.pace_dist_run, pacingModel.distRun.value)

                timeToLabel.text = getString(R.string.timeto, timeToString(resources, remainingTime))
                timeToProgress.progress = min(100, (100.0 - 100.0*(remainingTime / pacingModel.runTime)).toInt())
            }
        }

        setButton = paceView.buttonSet
        setButton.setOnClickListener {
            val resultBundle = Bundle()
            when (statusModel.pacingStatus.value) {
                PacingStatus.NotPacing -> {
                    pacingModel.setElapsedTime(0L)
                    pacingModel.resetWaypointProgress()

                    afm.setFragmentResult("BEGIN_PACING", resultBundle)
                }
                else -> throw IllegalStateException()
            }
        }

        goButton = paceView.buttonGo
        goButton.setOnClickListener {
            val resultBundle = Bundle()
            when (statusModel.pacingStatus.value) {
                PacingStatus.NotPacing    -> afm.setFragmentResult("BEGIN_PACING",  resultBundle)
                PacingStatus.PacingPaused -> afm.setFragmentResult("RESUME_PACING", resultBundle)
                PacingStatus.Pacing       -> afm.setFragmentResult("PAUSE_PACING",  resultBundle)
                else                      -> throw IllegalStateException()
            }
        }

        stopButton = paceView.buttonStop
        stopButton.setOnClickListener {
            afm.setFragmentResult("STOP_PACING",  Bundle())
        }

        pacingIcon = paceView.pacePacingStatus
        statusModel.pacingStatus.observe(viewLifecycleOwner) { pacingStatus: PacingStatus ->
            val ourContext = context ?: return@observe

            when(pacingStatus) {
                PacingStatus.NotPacing -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = true; setButton.isClickable = true

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false

                    val pacingIconId = if(statusModel.powerStart) R.drawable.power_stop_small else R.drawable.stop_small
                    pacingIcon.setImageDrawable(AppCompatResources.getDrawable(ourContext, pacingIconId))
                }

                PacingStatus.CheckPermissionStart -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.ServiceStart -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true
                }

                PacingStatus.PacingWait -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true

                    pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.power_small))
                }

                PacingStatus.PacingStart -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true
                }

                PacingStatus.Pacing -> {
                    setButton.visibility = View.GONE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.VISIBLE
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.pause))
                    goButton.isClickable = true; goButton.isEnabled = true

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true

                    pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.play_small))
                }

                PacingStatus.PacingPause -> {
                    setButton.visibility = View.GONE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.VISIBLE
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.pause2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.PacingPaused -> {
                    setButton.visibility = View.GONE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.VISIBLE
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.resume))
                    goButton.isEnabled = true; goButton.isClickable = true

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true

                    pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.pause_small))
                }

                PacingStatus.CheckPermissionResume -> {
                    setButton.visibility = View.GONE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.VISIBLE
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.resume2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.ServiceResume -> {
                    setButton.visibility = View.GONE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.VISIBLE
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.resume2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true
                }

                PacingStatus.PacingResume -> {
                    setButton.visibility = View.GONE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.VISIBLE
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.resume2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true
                }

                PacingStatus.PacingComplete -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.PacingCancel -> {
                    setButton.visibility = View.VISIBLE
                    setButton.isEnabled = false; setButton.isClickable = false

                    goButton.visibility = View.GONE
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }
            }
        }

        return paceView.root
    }

    override fun onResume() {
        super.onResume()

        val paceView     = binding!!
        val phoneIcon    = paceView.pacePhoneStatus
        val delaySetting = paceView.paceDelaySetting

        val powerStart = statusModel.powerStart
        val quickStart = statusModel.quickStart
        val startDelay = statusModel.startDelay

        val context = requireContext()
        when(statusModel.pacingStatus.value) {
            PacingStatus.NotPacing    -> {
                val pacingIconId = if(powerStart) R.drawable.power_stop_small else R.drawable.stop_small
                pacingIcon.setImageDrawable(AppCompatResources.getDrawable(context, pacingIconId))
            }

            PacingStatus.Pacing       -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.play_small))
            PacingStatus.PacingPaused -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.pause_small))
            else -> { }
        }

        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        val phoneIconId = if(phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, phoneIconId))

        val delayText = if(quickStart) "QCK" else if (powerStart) "PWR" else startDelay
        delaySetting.text = delayText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}