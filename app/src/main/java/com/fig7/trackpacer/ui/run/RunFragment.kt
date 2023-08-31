package com.fig7.trackpacer.ui.run

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.EditTimeDialog
import com.fig7.trackpacer.MainActivity
import com.fig7.trackpacer.PacingStatus
import com.fig7.trackpacer.R
import com.fig7.trackpacer.databinding.FragmentRunBinding
import com.fig7.trackpacer.distanceFor
import com.fig7.trackpacer.vm.RunViewModel

val RTMap = mapOf(
    "rt_400m_l1"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l1),
    "rt_400m_l2"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l2),
    "rt_400m_l3"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l3),
    "rt_400m_l4"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l4),
    "rt_400m_l5"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l5),
    "rt_400m_l6"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l6),
    "rt_400m_l7"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l7),
    "rt_400m_l8"   to arrayOf(R.string.laps_400, R.string.empty, R.string.empty, R.drawable.rt_400_l8),

    "rt_800m_l1"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l1),
    "rt_800m_l2"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l2),
    "rt_800m_l3"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l3),
    "rt_800m_l4"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l4),
    "rt_800m_l5"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l5),
    "rt_800m_l6"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l6),
    "rt_800m_l7"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l7),
    "rt_800m_l8"   to arrayOf(R.string.laps_800, R.string.ll_2, R.string.empty, R.drawable.rt_400_l8),

    "rt_1000m_l1"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l1),
    "rt_1000m_l2"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l2),
    "rt_1000m_l3"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l3),
    "rt_1000m_l4"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l4),
    "rt_1000m_l5"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l5),
    "rt_1000m_l6"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l6),
    "rt_1000m_l7"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l7),
    "rt_1000m_l8"  to arrayOf(R.string.laps_1000, R.string.fl_200m, R.string.ll_3, R.drawable.rt_1000_l8),

    "rt_1200m_l1"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l1),
    "rt_1200m_l2"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l2),
    "rt_1200m_l3"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l3),
    "rt_1200m_l4"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l4),
    "rt_1200m_l5"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l5),
    "rt_1200m_l6"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l6),
    "rt_1200m_l7"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l7),
    "rt_1200m_l8"  to arrayOf(R.string.laps_1200, R.string.ll_3, R.string.empty, R.drawable.rt_400_l8),

    "rt_1500m_l1"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l1),
    "rt_1500m_l2"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l2),
    "rt_1500m_l3"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l3),
    "rt_1500m_l4"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l4),
    "rt_1500m_l5"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l5),
    "rt_1500m_l6"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l6),
    "rt_1500m_l7"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l7),
    "rt_1500m_l8"  to arrayOf(R.string.laps_1500, R.string.fl_300m, R.string.ll_4, R.drawable.rt_1500_l8),

    "rt_2000m_l1"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l1),
    "rt_2000m_l2"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l2),
    "rt_2000m_l3"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l3),
    "rt_2000m_l4"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l4),
    "rt_2000m_l5"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l5),
    "rt_2000m_l6"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l6),
    "rt_2000m_l7"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l7),
    "rt_2000m_l8"  to arrayOf(R.string.laps_2000, R.string.ll_5, R.string.empty, R.drawable.rt_400_l8),

    "rt_3000m_l1"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l1),
    "rt_3000m_l2"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l2),
    "rt_3000m_l3"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l3),
    "rt_3000m_l4"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l4),
    "rt_3000m_l5"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l5),
    "rt_3000m_l6"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l6),
    "rt_3000m_l7"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l7),
    "rt_3000m_l8"  to arrayOf(R.string.laps_3000, R.string.fl_200m, R.string.ll_8, R.drawable.rt_1000_l8),

    "rt_4000m_l1"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l1),
    "rt_4000m_l2"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l2),
    "rt_4000m_l3"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l3),
    "rt_4000m_l4"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l4),
    "rt_4000m_l5"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l5),
    "rt_4000m_l6"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l6),
    "rt_4000m_l7"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l7),
    "rt_4000m_l8"  to arrayOf(R.string.laps_4000, R.string.ll_10, R.string.empty, R.drawable.rt_400_l8),

    "rt_5000m_l1"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l1),
    "rt_5000m_l2"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l2),
    "rt_5000m_l3"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l3),
    "rt_5000m_l4"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l4),
    "rt_5000m_l5"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l5),
    "rt_5000m_l6"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l6),
    "rt_5000m_l7"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l7),
    "rt_5000m_l8"  to arrayOf(R.string.laps_5000, R.string.fl_200m, R.string.ll_13, R.drawable.rt_1000_l8),

    "rt_10000m_l1" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l1),
    "rt_10000m_l2" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l2),
    "rt_10000m_l3" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l3),
    "rt_10000m_l4" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l4),
    "rt_10000m_l5" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l5),
    "rt_10000m_l6" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l6),
    "rt_10000m_l7" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l7),
    "rt_10000m_l8" to arrayOf(R.string.laps_10000, R.string.ll_25, R.string.empty, R.drawable.rt_400_l8),

    "rt_1 mile_l1" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l1),
    "rt_1 mile_l2" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l2),
    "rt_1 mile_l3" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l3),
    "rt_1 mile_l4" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l4),
    "rt_1 mile_l5" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l5),
    "rt_1 mile_l6" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l6),
    "rt_1 mile_l7" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l7),
    "rt_1 mile_l8" to arrayOf(R.string.laps_mile, R.string.fl_mile, R.string.ll_4, R.drawable.rt_mile_l8))

class RunFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var afm: FragmentManager
    private val runViewModel: RunViewModel by activityViewModels()

    private var binding: FragmentRunBinding? = null
    private lateinit var spinnerDistance: Spinner
    private lateinit var spinnerLane: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var spinnerProfile: Spinner

    private lateinit var editButton: ImageButton
    private lateinit var goButton: ImageButton
    private lateinit var stopButton: ImageButton

    private fun runTimeFromSpinner(): Double {
        val runTime = spinnerTime.selectedItem.toString()
        val runTimeSplit = runTime.split(":")
        return 1000.0*(runTimeSplit[0].trim().toLong()*60.0 + runTimeSplit[1].toDouble())
    }

    private fun enableSpinners(enabled: Boolean) {
        spinnerDistance.isEnabled   = enabled
        spinnerDistance.isClickable = enabled

        spinnerLane.isEnabled   = enabled
        spinnerLane.isClickable = enabled

        spinnerTime.isEnabled   = enabled
        spinnerTime.isClickable = enabled

        spinnerProfile.isEnabled   = enabled
        spinnerProfile.isClickable = enabled

        editButton.isEnabled   = enabled
        editButton.isClickable = enabled
    }

    private fun updateTimeSpinner(timeIndex: Int = -1) {
        val dataManager = runViewModel.dataManager
        val runDist = spinnerDistance.selectedItem.toString()

        val spinnerTimeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, dataManager.timeMap[runDist]!!)
        spinnerTimeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTime.adapter = spinnerTimeAdapter
        if (timeIndex != -1) spinnerTime.setSelection(timeIndex)
    }

    private fun updateTrackOverlay() {
        val runDist = spinnerDistance.selectedItem.toString()
        val runLane = spinnerLane.selectedItem.toString()

        val overlayDesc  = "rt_" + runDist + "_l" + runLane
        val overlayArray = RTMap[overlayDesc]!!
        val runView = binding!!

        val runLaneInt= runLane.toInt()
        val totalDist = distanceFor(runDist, runLaneInt)
        val totalDistStr =
            if(runDist == "1 mile") {
                if (runLaneInt == 1) runDist else String.format("%.2f miles", totalDist/1609.34)
            } else {
                if (runLaneInt == 1) String.format("%dm", totalDist.toInt()) else String.format("%.2fm", totalDist)
            }

        val labelSF = runView.labelStartFinish
        labelSF.text = getString(R.string.label_start, totalDistStr)

        val lapCounter = runView.labelLaps
        lapCounter.text = getString(overlayArray[0])

        val lapDesc1 = runView.labelLapDesc1
        lapDesc1.text = getString(overlayArray[1])

        val lastDesc2 = runView.labelLapDesc2
        lastDesc2.text = getString(overlayArray[2])

        val trackOverlay = runView.runningTrackOverlay
        trackOverlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), overlayArray[3]))
    }

    private fun updatePacingStatus(pacingStatus: PacingStatus) {
        val runView = binding!!
        val pacingIcon = runView.pacingStatus
        when (pacingStatus) {
            PacingStatus.NotPacing    -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.stop_small))
            PacingStatus.Pacing       -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.play_small))
            PacingStatus.PacingPaused -> pacingIcon.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.pause_small))
            else -> { }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRunBinding.inflate(inflater, container, false)

        val mainActivity    = activity as MainActivity
        afm = mainActivity.supportFragmentManager

        val fragmentContext = requireContext()
        val dataManager     = runViewModel.dataManager
        val runView         = binding!!

        val timerView = runView.textTime
        timerView.text = savedInstanceState?.getString("TIMER_VAL") ?: getString(R.string.base_time_all, "00", "00", "00", "000")

        spinnerDistance = runView.spinnerDistance
        val spinnerDistanceAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, dataManager.distanceArray)
        spinnerDistanceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerDistance.adapter = spinnerDistanceAdapter

        savedInstanceState?.run { val spinnerDistancePos = getInt("SP_DISTANCE"); spinnerDistance.setSelection(spinnerDistancePos)  }
        spinnerDistance.onItemSelectedListener = this

        spinnerLane = runView.spinnerLane
        val laneArray: Array<String> = resources.getStringArray(R.array.lane_array)
        val spinnerLaneAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, laneArray)
        spinnerLaneAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerLane.adapter = spinnerLaneAdapter

        savedInstanceState?.run { val spinnerLanePos = getInt("SP_LANE"); spinnerLane.setSelection(spinnerLanePos)  }
        spinnerLane.onItemSelectedListener = this

        spinnerTime = runView.spinnerTime
        val spinnerTimeAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, dataManager.timeMap[spinnerDistance.selectedItem.toString()]!!)
        spinnerTimeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTime.adapter = spinnerTimeAdapter

        savedInstanceState?.run { spinnerTime.setSelection(getInt("SP_TIME")) }
        runViewModel.selectedTime.observe(viewLifecycleOwner) { newIndex: Int ->
            updateTimeSpinner(newIndex)
        }

        spinnerProfile = runView.spinnerProfile
        val profileArray: Array<String> = resources.getStringArray(R.array.profile_array)
        val spinnerProfileAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, profileArray)
        spinnerProfileAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerProfile.adapter = spinnerProfileAdapter
        savedInstanceState?.run { spinnerTime.setSelection(getInt("SP_PROFILE")) }

        val nextUpLabel = runView.nextupLabel
        nextUpLabel.text = savedInstanceState?.getString("NEXTUP_LABEL") ?: getString(R.string.nextup, "")

        val nextUpProgress = runView.nextupProgress
        nextUpProgress.progress = savedInstanceState?.getInt("NEXTUP_PROGRESS") ?: 0

        val timeToLabel = runView.timetoLabel
        timeToLabel.text = savedInstanceState?.getString("TIMETO_LABEL") ?: getString(R.string.timeto, "")

        val timeToProgress = runView.timetoProgress
        timeToProgress.progress = savedInstanceState?.getInt("TIMETO_PROGRESS") ?: 0

        val pacingStatus = mainActivity.pacingStatus
        editButton = runView.buttonTime
        editButton.setOnClickListener {
            val dialog = EditTimeDialog.newDialog(spinnerTime.selectedItem.toString(), dataManager.timeMap[spinnerDistance.selectedItem.toString()]!!, "EDIT_TIME_DIALOG")
            parentFragmentManager.setFragmentResultListener("EDIT_TIME_DIALOG", this) { requestKey: String, result: Bundle ->
                parentFragmentManager.clearFragmentResult(requestKey)

                result.putString("EditDist", spinnerDistance.selectedItem.toString())
                afm.setFragmentResult(requestKey, result)
        }

            dialog.show(parentFragmentManager, "EDIT_TIME_DIALOG")
        }

        goButton = runView.buttonGo
        goButton.setOnClickListener {
            when (pacingStatus) {
                PacingStatus.NotPacing    -> {
                    val beginPacingBundle = Bundle()
                    beginPacingBundle.putString("RUN_DISTANCE", spinnerDistance.selectedItem.toString())
                    beginPacingBundle.putInt("RUN_LANE",        spinnerLane.selectedItem.toString().toInt())
                    beginPacingBundle.putDouble("RUN_TIME",     runTimeFromSpinner())
                    afm.setFragmentResult("BEGIN_PACING", beginPacingBundle)
                }
                PacingStatus.PacingPaused -> { /* mainActivity.resumePacing() */ }
                PacingStatus.Pacing       -> { /* mainActivity.pausePacing(false) */ }
                else                      -> throw IllegalStateException()
            }
        }

        stopButton = runView.buttonStop
        stopButton.setOnClickListener {
            /* mainActivity.stopPacing(false) */
        }

        when (pacingStatus) {
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
        }

        return runView.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        updateTimeSpinner()
        updateTrackOverlay()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }
}
