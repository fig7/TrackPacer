package com.fig7.trackpacer.ui.pace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.PacingActivity
import com.fig7.trackpacer.PacingStatus
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.PacingModel
import com.fig7.trackpacer.databinding.FragmentPaceBinding
import com.fig7.trackpacer.distanceFor
import com.fig7.trackpacer.timeFor
import kotlin.math.abs

class PaceFragment: Fragment() {
    private lateinit var afm: FragmentManager
    private var binding: FragmentPaceBinding? = null
    private val pacingModel: PacingModel by activityViewModels()

    private lateinit var nextUpLabel: TextView
    private lateinit var nextUpProgress: ProgressBar

    private lateinit var timeToLabel: TextView
    private lateinit var timeToProgress: ProgressBar

    private lateinit var goButton: ImageButton
    private lateinit var stopButton: ImageButton

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

    private fun timeToAlmostFullString(timeInMS: Long): String {
        var timeLeft = timeInMS

        val hrs = timeLeft / 3600000L
        timeLeft -= hrs * 3600000L

        val mins = timeLeft / 60000L
        timeLeft -= mins * 60000L

        val secs = timeLeft / 1000L
        timeLeft -= secs * 1000L

        val msStr = String.format("%02d", timeLeft/10L)
        return if(hrs > 0) {
            val hrsStr = String.format("%d", hrs)
            val minsStr = String.format("%02d", mins)
            val secsStr = String.format("%02d", secs)
            getString(R.string.base_time_all, "", hrsStr, minsStr, secsStr, msStr)
        } else {
            val minsStr = String.format("%d", mins)
            val secsStr = String.format("%02d", secs)
            getString(R.string.base_time_mss, minsStr, secsStr, msStr)
        }
    }

    private fun timeToFullString(timeInMS: Long): String {
        var timeLeft = abs(timeInMS)
        val sgnStr  = if (timeInMS < 0) "-" else ""

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
        return getString(R.string.base_time_all, sgnStr, hrsStr, minsStr, secsStr, msStr)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPaceBinding.inflate(inflater, container, false)
        val paceView = binding!!

        val mainActivity = activity as PacingActivity
        afm = mainActivity.supportFragmentManager

        val timerView = paceView.textTime
        timerView.text = savedInstanceState?.getString("TIMER_VAL") ?: timeToFullString(0L)
        pacingModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime: Long ->
            timerView.text = timeToFullString(elapsedTime)
        }

        val runDist = pacingModel.runDist
        val runLane = pacingModel.runLane
        val runTime = pacingModel.runTime
        val totalDist = distanceFor(runDist, runLane)
        val totalDistStr =
            if(runDist == "1 mile") {
                if(runLane == 1) runDist else String.format("%.2f miles", totalDist/1609.34)
            } else {
                if (runLane == 1) String.format("%dm", totalDist.toInt()) else String.format("%.2fm", totalDist)
            }

        val totalTime = timeFor(runDist, runLane, runTime)
        val totalTimeStr = timeToAlmostFullString(totalTime.toLong())

        val totalPace = (1000.0 * totalTime) / totalDist
        val totalPaceStr = timeToString(totalPace.toLong())

        val runDistLabel = paceView.labelPacingDist
        runDistLabel.text = getString(R.string.label_distance2, runLane)

        val runDistView = paceView.pacingDist
        runDistView.text = getString(R.string.pace_dist, totalDistStr, pacingModel.runLaps)

        val runProfView = paceView.pacingProf
        runProfView.text = pacingModel.runProf

        val runTimeView = paceView.pacingTime
        runTimeView.text = getString(R.string.pace_pace, totalTimeStr, totalPaceStr)

        nextUpLabel = paceView.nextupLabel
        nextUpLabel.text = savedInstanceState?.getString("NEXTUP_LABEL") ?: getString(R.string.nextup, "")

        nextUpProgress = paceView.nextupProgress
        nextUpProgress.progress = savedInstanceState?.getInt("NEXTUP_PROGRESS") ?: 0

        timeToLabel = paceView.timetoLabel
        timeToLabel.text = savedInstanceState?.getString("TIMETO_LABEL") ?: getString(R.string.timeto, "")

        timeToProgress = paceView.timetoProgress
        timeToProgress.progress = savedInstanceState?.getInt("TIMETO_PROGRESS") ?: 0

        pacingModel.pacingStatus.observe(viewLifecycleOwner) { pacingStatus: PacingStatus ->
            val ourContext = context ?: return@observe

            when(pacingStatus) {
                PacingStatus.NotPacing -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.play))
                    goButton.isEnabled = true; goButton.isClickable = true

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.CheckPermissionStart -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.play2))
                    goButton.isEnabled = false; goButton.isClickable = false
                }

                PacingStatus.ServiceStart -> {
                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true

                    pacingModel.resetWaypointProgress()
                }

                PacingStatus.PacingStart ->  { }
                PacingStatus.PacingResume -> { }
                PacingStatus.Pacing -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.pause))
                    goButton.isClickable = true; goButton.isEnabled = true
                }

                PacingStatus.PacingPause -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.pause2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.PacingPaused -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.resume))
                    goButton.isEnabled = true; goButton.isClickable = true

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true
                }

                PacingStatus.CheckPermissionResume -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.resume2))
                    goButton.isEnabled = false; goButton.isClickable = false
                }

                PacingStatus.ServiceResume -> {
                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop))
                    stopButton.isEnabled = true; stopButton.isClickable = true
                }

                PacingStatus.PacingCancel -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.play2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }

                PacingStatus.PacingComplete -> {
                    goButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.play2))
                    goButton.isEnabled = false; goButton.isClickable = false

                    stopButton.setImageDrawable(AppCompatResources.getDrawable(ourContext, R.drawable.stop2))
                    stopButton.isEnabled = false; stopButton.isClickable = false
                }
            }
        }

        goButton = paceView.buttonGo
        goButton.setOnClickListener {
            val resultBundle = Bundle()
            when (pacingModel.pacingStatus.value) {
                PacingStatus.NotPacing    -> afm.setFragmentResult("BEGIN_PACING",  resultBundle)
                PacingStatus.PacingPaused -> afm.setFragmentResult("RESUME_PACING", resultBundle)
                PacingStatus.Pacing       -> afm.setFragmentResult("PAUSE_PACING",  resultBundle)
                else                      -> throw IllegalStateException()
            }
        }

        // Do live updates for go button
        // No longer needed. When not pacing these items are not visible!
        /* when (pacingStatus) {
            PacingStatus.NotPacing -> {
                goButton.setImageDrawable(AppCompatResources.getDrawable(fragmentContext, R.drawable.play))
                goButton.isEnabled = true
                goButton.isClickable = true

                stopButton.setImageDrawable(AppCompatResources.getDrawable(fragmentContext, R.drawable.stop2))
                stopButton.isEnabled = false
                stopButton.isClickable = false

                enableSpinners(true)
            }
            PacingStatus.PacingPaused -> {
                goButton.setImageDrawable(AppCompatResources.getDrawable(fragmentContext, R.drawable.resume))
                goButton.isEnabled = true
                goButton.isClickable = true

                stopButton.setImageDrawable(AppCompatResources.getDrawable(fragmentContext, R.drawable.stop))
                stopButton.isEnabled = true
                stopButton.isClickable = true

                enableSpinners(false)
            }
            else -> throw IllegalStateException()
        } */

        stopButton = paceView.buttonStop
        stopButton.setOnClickListener {
            afm.setFragmentResult("STOP_PACING",  Bundle())
        }

        // Do live updates for stop button

        return paceView.root
    }


        // Potentially re-implementing this in the pace view is ok.
    /* private fun updatePacingStatus(pacingStatus: PacingStatus) {
        val pacingView = binding!!
        val pacingIcon = pacingView.pacingStatus
        when (pacingStatus) {
            PacingStatus.NotPacing    -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.stop_small))
            PacingStatus.Pacing       -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.play_small))
            PacingStatus.PacingPaused -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.pause_small))
            else -> { }
        }
    } */
}