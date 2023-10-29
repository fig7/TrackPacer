package com.fig7.trackpacer.ui.completion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.CompletionActivity
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.FragmentCompletionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompletionFragment: Fragment() {
    private var binding: FragmentCompletionBinding? = null
    private lateinit var afm: FragmentManager

    private val resultModel: ResultModel by activityViewModels()
    private val statusModel: StatusModel by activityViewModels()

    private lateinit var completionDate: TextView
    private lateinit var completionDistLabel: TextView
    private lateinit var completionDist: TextView
    private lateinit var completionTgtTime: TextView
    private lateinit var completionActTime: TextView
    private lateinit var completionDiff: TextView
    private lateinit var completionNotes: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompletionBinding.inflate(inflater, container, false)
        val completionView = binding!!

        val mainActivity = activity as CompletionActivity
        afm = mainActivity.supportFragmentManager

        val resultData = resultModel.resultData
        val netDate = Date(resultData.runDate)

        completionDate = completionView.completionDate
        completionDate.text = SimpleDateFormat("d MMM, yyyy 'at' HH:mm", Locale.getDefault()).format(netDate)

        completionDistLabel = completionView.labelCompletionDist
        completionDistLabel.text = getString(R.string.completion_dist_label, resultData.runProf)

        completionDist = completionView.completionDist
        completionDist.text = getString(R.string.completion_dist, resultData.totalDistStr, resultData.runDist, resultData.runLane)

        completionTgtTime = completionView.completionTgtTime
        completionTgtTime.text = getString(R.string.pace_pace, resultData.totalTimeStr, resultData.totalPaceStr)

        completionActTime = completionView.completionActTime
        completionActTime.text = getString(R.string.pace_pace, resultData.actualTimeStr, resultData.actualPaceStr)

        completionDiff = completionView.completionDiff
        completionDiff.text = resultData.earlyLateStr

        completionNotes = completionView.completionNotes

        val saveButton = completionView.buttonSave
        saveButton.setOnClickListener {
            resultData.runNotes = completionNotes.text.toString()

            val resultBundle = Bundle()
            resultBundle.putParcelable("resultParcel", resultData)
            afm.setFragmentResult("SAVE_ME", resultBundle)
        }

        val closeButton = completionView.buttonClose
        closeButton.setOnClickListener { afm.setFragmentResult("CLOSE_ME", Bundle()) }

        return completionView.root
    }

    override fun onResume() {
        super.onResume()

        val completionView = binding!!
        val pacingIcon = completionView.completionPacingStatus
        val phoneIcon = completionView.completionPhoneStatus
        val delaySetting = completionView.completionDelaySetting

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