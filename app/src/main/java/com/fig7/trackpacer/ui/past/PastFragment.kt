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
import com.fig7.trackpacer.databinding.FragmentPastBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PastFragment: Fragment() {
    private var binding: FragmentPastBinding? = null
    private val resultModel: ResultModel by activityViewModels()

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

        val pastView = binding!!
        val context = requireContext()

        val phoneIcon = pastView.pastPhoneStatus
        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = pastView.pastDelaySetting
        delaySetting.setText(R.string.start_delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}