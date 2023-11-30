package com.fig7.trackpacer.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.R
import com.fig7.trackpacer.enums.FMRResult
import com.google.android.material.switchmaterial.SwitchMaterial

class FMRDialog: DialogFragment() {
    private lateinit var dialogTag: String

    private lateinit var dialogSwitch: SwitchMaterial

    private lateinit var positiveButton: Button
    private lateinit var negativeButton: Button

    companion object {
        fun newDialog(tag: String): FMRDialog {
            val f = FMRDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("tag", tag)
            f.arguments = args

            return f
        }
    }

    private fun cancelFMR() {
        val dialogResult = Bundle()
        dialogResult.putInt("FMRResult", FMRResult.Cancel.ordinal)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    private fun negativeFMR() {
        val dialogResult = Bundle()
        dialogResult.putInt("FMRResult",  FMRResult.Settings.ordinal)
        dialogResult.putBoolean("FMRDisable", dialogSwitch.isChecked)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    private fun positiveFMR() {
        val dialogResult = Bundle()
        dialogResult.putInt("FMRResult", FMRResult.Run.ordinal)
        dialogResult.putBoolean("FMRDisable", dialogSwitch.isChecked)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()

        dialogTag = args.getString("tag").toString()

        val v: View = inflater.inflate(R.layout.dialog_fmr, container, false)

        val cancelButton = v.findViewById<ImageButton>(R.id.fmr_cancel)
        cancelButton.setOnClickListener { cancelFMR() }

        dialogSwitch = v.findViewById(R.id.fmr_switch)

        negativeButton = v.findViewById(R.id.fmr_negative)
        negativeButton.setOnClickListener { negativeFMR() }

        positiveButton = v.findViewById(R.id.fmr_positive)
        positiveButton.setOnClickListener { positiveFMR() }

        return v
    }
}