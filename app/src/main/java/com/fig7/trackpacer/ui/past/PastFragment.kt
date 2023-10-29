package com.fig7.trackpacer.ui.past

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.FragmentPastBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PastFragment: Fragment() {
    private var binding: FragmentPastBinding? = null

    private val resultModel: ResultModel by activityViewModels()
    private val statusModel: StatusModel by activityViewModels()

    private lateinit var pastDate: TextView
    private lateinit var pastDistLabel: TextView
    private lateinit var pastDist: TextView
    private lateinit var pastTgtTime: TextView
    private lateinit var pastActTime: TextView
    private lateinit var pastDiff: TextView
    private lateinit var pastNotes: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPastBinding.inflate(inflater, container, false)
        val pastView = binding!!

        val resultData = resultModel.resultData
        val netDate = Date(resultData.runDate)

        pastDate = pastView.pastDate
        pastDate.text = SimpleDateFormat("d MMM, yyyy 'at' HH:mm", Locale.getDefault()).format(netDate)

        pastDistLabel = pastView.labelPastDist
        pastDistLabel.text = getString(R.string.completion_dist_label, resultData.runProf)

        pastDist = pastView.pastDist
        pastDist.text = getString(R.string.completion_dist, resultData.totalDistStr, resultData.runDist, resultData.runLane)

        pastTgtTime = pastView.pastTgtTime
        pastTgtTime.text = getString(R.string.pace_pace, resultData.totalTimeStr, resultData.totalPaceStr)

        pastActTime = pastView.pastActTime
        pastActTime.text = getString(R.string.pace_pace, resultData.actualTimeStr, resultData.actualPaceStr)

        pastDiff = pastView.pastDiff
        pastDiff.text = resultData.earlyLateStr

        pastNotes = pastView.pastNotes
        pastNotes.text = resultData.runNotes.ifEmpty { "*No notes added*" }

        return pastView.root
    }

    override fun onResume() {
        super.onResume()

        val pastView     = binding!!
        val pacingIcon   = pastView.pastPacingStatus
        val phoneIcon    = pastView.pastPhoneStatus
        val delaySetting = pastView.pastDelaySetting

        val powerStart = statusModel.powerStart
        val quickStart = statusModel.quickStart
        val startDelay = statusModel.startDelay

        val context = requireContext()
        val pacingIconId = if(powerStart) R.drawable.power_stop_small else R.drawable.stop_small
        pacingIcon.setImageDrawable(AppCompatResources.getDrawable(context, pacingIconId))

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