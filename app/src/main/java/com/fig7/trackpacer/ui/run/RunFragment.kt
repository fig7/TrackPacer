package com.fig7.trackpacer.ui.run

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.fig7.trackpacer.BuildConfig
import com.fig7.trackpacer.dialog.EditTimeDialog
import com.fig7.trackpacer.dialog.InfoDialog
import com.fig7.trackpacer.MainActivity
import com.fig7.trackpacer.PacingActivity
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.StorageModel
import com.fig7.trackpacer.databinding.FragmentRunBinding
import com.fig7.trackpacer.waypoint.distanceFor

val rtMap = mapOf(
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

private fun rtLaps(runDist: String, runLane: Int): Int {
    val rtKey   = "rt_" + runDist + "_l" + runLane.toString()
    val rtArray = rtMap[rtKey]!!
    return rtArray[0]
}

private fun rtDesc1(runDist: String, runLane: Int): Int {
    val rtKey   = "rt_" + runDist + "_l" + runLane.toString()
    val rtArray = rtMap[rtKey]!!
    return rtArray[1]
}

private fun rtDesc2(runDist: String, runLane: Int): Int {
    val rtKey  = "rt_" + runDist + "_l" + runLane.toString()
    val rtArray = rtMap[rtKey]!!
    return rtArray[2]
}

private fun rtOverlay(runDist: String, runLane: Int): Int {
    val rtKey  = "rt_" + runDist + "_l" + runLane.toString()
    val rtArray = rtMap[rtKey]!!
    return rtArray[3]
}

class RunFragment: Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var afm: FragmentManager
    private var binding: FragmentRunBinding? = null

    private val storageModel: StorageModel by activityViewModels()
    private val runViewModel:  RunViewModel  by activityViewModels()
    private var initializingSpinners = 0

    private lateinit var spinnerDist: Spinner
    private lateinit var spinnerLane: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var spinnerProfile: Spinner

    private lateinit var editTimeButton: ImageButton
    private lateinit var editProfileButton: ImageButton

    private fun runTimeFromSpinner(): Double {
        val runTime = spinnerTime.selectedItem.toString()
        val runTimeSplit = runTime.split(":")
        return 1000.0*(runTimeSplit[0].trim().toLong()*60.0 + runTimeSplit[1].toDouble())
    }

    private fun updateTimeSpinner(newIndex: Int = 0) {
        val dataManager = storageModel.storageManager
        val runDist = spinnerDist.selectedItem.toString()

        val spinnerTimeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, dataManager.timeMap[runDist]!!)
        spinnerTimeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTime.adapter = spinnerTimeAdapter
        spinnerTime.setSelection(newIndex)
    }

    private fun updateTrackOverlay() {
        val runDist = spinnerDist.selectedItem.toString()
        val runLane = spinnerLane.selectedItem.toString().toInt()
        val runView = binding!!

        val totalDist = distanceFor(runDist, runLane)
        val totalDistStr =
            if(runDist == "1 mile") {
                if (runLane == 1) runDist else String.format("%.2f miles", totalDist/1609.34)
            } else {
                if (runLane == 1) String.format("%dm", totalDist.toInt()) else String.format("%.2fm", totalDist)
            }

        val labelSF = runView.labelStartFinish
        labelSF.text = getString(R.string.label_start, totalDistStr)

        val lapCounter = runView.labelLaps
        lapCounter.text = getString(rtLaps(runDist, runLane))

        val lapDesc1 = runView.labelLapDesc1
        lapDesc1.text = getString(rtDesc1(runDist, runLane))

        val lastDesc2 = runView.labelLapDesc2
        lastDesc2.text = getString(rtDesc2(runDist, runLane))

        val trackOverlay = runView.runningTrackOverlay
        trackOverlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), rtOverlay(runDist, runLane)))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRunBinding.inflate(inflater, container, false)

        val mainActivity = activity as MainActivity
        afm = mainActivity.supportFragmentManager

        val fragmentContext = requireContext()
        val dataManager     = storageModel.storageManager
        val runView         = binding!!

        initializingSpinners = 3
        val spinnerDistSelection = runViewModel.selectedDist.value!!
        val spinnerLaneSelection = runViewModel.selectedLane.value!!
        val spinnerTimeSelection = runViewModel.selectedTime.value!!
        val spinnerDistStr = dataManager.distanceArray[spinnerDistSelection]

        val spinnerDistAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, dataManager.distanceArray)
        spinnerDistAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinnerDist = runView.spinnerDistance
        spinnerDist.adapter = spinnerDistAdapter
        spinnerDist.setSelection(spinnerDistSelection)

        val laneArray: Array<String> = resources.getStringArray(R.array.lane_array)
        val spinnerLaneAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, laneArray)
        spinnerLaneAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinnerLane = runView.spinnerLane
        spinnerLane.adapter = spinnerLaneAdapter
        spinnerLane.setSelection(spinnerLaneSelection)

        val spinnerTimeAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, dataManager.timeMap[spinnerDistStr]!!)
        spinnerTimeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinnerTime = runView.spinnerTime
        spinnerTime.adapter = spinnerTimeAdapter
        spinnerLane.setSelection(spinnerTimeSelection)

        spinnerProfile = runView.spinnerProfile
        val profileArray: Array<String> = resources.getStringArray(R.array.profile_array)
        val spinnerProfileAdapter = ArrayAdapter(fragmentContext, R.layout.spinner_item, profileArray)
        spinnerProfileAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerProfile.adapter = spinnerProfileAdapter

        spinnerDist.onItemSelectedListener = this
        spinnerLane.onItemSelectedListener = this
        spinnerTime.onItemSelectedListener = this
        runViewModel.selectedTime.observe(viewLifecycleOwner) { newIndex: Int ->
            updateTimeSpinner(newIndex)
        }

        editTimeButton = runView.buttonTime
        editTimeButton.setOnClickListener {
            val dialog = EditTimeDialog.newDialog(spinnerTime.selectedItem.toString(), dataManager.timeMap[spinnerDist.selectedItem.toString()]!!, "EDIT_TIME_DIALOG")
            parentFragmentManager.setFragmentResultListener("EDIT_TIME_DIALOG", this) { requestKey: String, result: Bundle ->
                parentFragmentManager.clearFragmentResult(requestKey)

                result.putString("EditDist", spinnerDist.selectedItem.toString())
                afm.setFragmentResult("EDIT_TIME", result)
            }

            dialog.show(parentFragmentManager, "EDIT_TIME_DIALOG")
        }

        editProfileButton = runView.buttonProfile

        @Suppress("KotlinConstantConditions")
        if(BuildConfig.FLAVOR == "free") {
            editProfileButton.setOnClickListener {
                val dialog = InfoDialog.newDialog("Create profiles",
                    "Profiles are a feature that allow you to incorporate changes to your pace. "
                            +"Perhaps you want to run at a slower pace, or stop to recover, between fast intervals. Speed up and slow down, you decide when!\n\n"
                            +"Profiles will only be available in the pro version of TrackPacer, which is coming soon...")

                dialog.show(parentFragmentManager, "PROFILE_PRO_DIALOG")
            }
        }

        val oymButton = runView.buttonOym
        oymButton.setOnClickListener {
            val bundle = Bundle()
            val runDist = spinnerDist.selectedItem.toString()
            bundle.putString("RunDist", runDist)

            val runLane = spinnerLane.selectedItem.toString().toInt()
            bundle.putInt("RunLane", runLane)

            val runProf = spinnerProfile.selectedItem.toString()
            bundle.putString("RunProf", runProf)

            val runLaps = rtLaps(runDist, runLane)
            bundle.putString("RunLaps", getString(runLaps))

            val runTime = runTimeFromSpinner()
            bundle.putDouble("RunTime", runTime)

            val intent = Intent(requireContext(), PacingActivity::class.java)
            intent.putExtras(bundle)

            startActivity(intent)
        }

        updateTrackOverlay()
        return runView.root
    }

    override fun onResume() {
        super.onResume()

        val runView = binding!!
        val context = requireContext()

        val phoneIcon = runView.runPhoneStatus
        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = runView.runDelaySetting
        delaySetting.setText(R.string.start_delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if(p0 == spinnerDist) {
            runViewModel.selectDist(spinnerDist.selectedItemPosition)

            if(initializingSpinners == 0) {
                updateTimeSpinner()
            }
        }

        if(p0 == spinnerLane) {
            runViewModel.selectLane(spinnerLane.selectedItemPosition)
        }

        if(p0 == spinnerTime) {
            runViewModel.selectTime(spinnerTime.selectedItemPosition)
        }

        if(initializingSpinners > 0) {
            --initializingSpinners
            return
        }

        updateTrackOverlay()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }
}
