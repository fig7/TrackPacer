package com.fig7.trackpacer.ui.completion

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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.CompletionActivity
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.databinding.FragmentCompletionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompletionFragment: Fragment() {
    private var binding: FragmentCompletionBinding? = null
    private lateinit var afm: FragmentManager

    private val resultModel: ResultModel by activityViewModels()

    private lateinit var completionDate: TextView
    private lateinit var completionDistLabel: TextView
    private lateinit var completionDist: TextView
    private lateinit var completionTgtTime: TextView
    private lateinit var completionActTime: TextView
    private lateinit var completionDiff: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompletionBinding.inflate(inflater, container, false)
        val completionView = binding!!

        val mainActivity = activity as CompletionActivity
        afm = mainActivity.supportFragmentManager

        val df = SimpleDateFormat("d MMM, yyyy 'at' HH:mm", Locale.getDefault())
        val netDate = Date(resultModel.startTime)

        completionDate = completionView.completionDate
        completionDate.text = df.format(netDate)

        completionDistLabel = completionView.labelCompletionDist
        completionDistLabel.text = getString(R.string.completion_dist_label, resultModel.runProf)

        completionDist = completionView.completionDist
        completionDist.text = getString(R.string.completion_dist, resultModel.totalDistStr, resultModel.runDist, resultModel.runLane)

        completionTgtTime = completionView.completionTgtTime
        completionTgtTime.text = getString(R.string.pace_pace, resultModel.totalTimeStr, resultModel.totalPaceStr)

        completionActTime = completionView.completionActTime
        completionActTime.text = getString(R.string.pace_pace, resultModel.actualTimeStr, resultModel.actualPaceStr)

        completionDiff = completionView.completionDiff
        completionDiff.text = resultModel.earlyLateStr

        val closeButton = completionView.buttonClose
        closeButton.setOnClickListener { afm.setFragmentResult("CLOSE_ME", Bundle()) }

        return completionView.root
    }

    override fun onResume() {
        super.onResume()

        val completionView = binding!!
        val context = requireContext()

        val phoneIcon = completionView.completionPhoneStatus
        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = completionView.completionDelaySetting
        delaySetting.setText(R.string.start_delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}