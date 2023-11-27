package com.fig7.trackpacer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.R
import com.google.android.material.switchmaterial.SwitchMaterial

class FMRDialog: DialogFragment() {
    private lateinit var dialogTitle: String
    private lateinit var dialogMessage: String
    private lateinit var dialogPositive: String
    private lateinit var dialogNegative: String
    private lateinit var dialogTag: String

    private lateinit var dialogSwitch: SwitchMaterial

    companion object {
        fun newDialog(title: String, message: String, positive: String, negative: String, tag: String): FMRDialog {
            val f = FMRDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("title",    title)
            args.putString("message",  message)
            args.putString("positive", positive)
            args.putString("negative", negative)
            args.putString("tag",      tag)
            f.arguments = args
            return f
        }
    }

    private fun setResult(resultVal: Boolean, disableReminder: Boolean) {
        val dialogResult = Bundle()
        dialogResult.putBoolean("FMRResult",  resultVal)
        dialogResult.putBoolean("FMRDisable", disableReminder)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()

        dialogTitle    = args.getString("title").toString()
        dialogMessage  = args.getString("message").toString()
        dialogPositive = args.getString("positive").toString()
        dialogNegative = args.getString("negative").toString()
        dialogTag      = args.getString("tag").toString()

        val builder = AlertDialog.Builder(activity)
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_fmr, null, false)
        val dialogText: TextView = dialogView.findViewById(R.id.fmr_message)

        dialogText.text = dialogMessage
        dialogSwitch = dialogView.findViewById(R.id.fmr_switch)

        builder.setTitle(dialogTitle)
        builder.setView(dialogView)
        builder.setPositiveButton(dialogPositive) { _, _ -> setResult(true, dialogSwitch.isChecked) }
        builder.setNegativeButton(dialogNegative) { _, _ -> setResult(false, dialogSwitch.isChecked) }
        return builder.create()
    }
}
