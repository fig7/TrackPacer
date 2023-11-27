package com.fig7.trackpacer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class QuestionDialog: DialogFragment() {
    private lateinit var dialogTitle: String
    private lateinit var dialogMessage: String
    private lateinit var dialogPositive: String
    private lateinit var dialogNegative: String
    private lateinit var dialogTag: String

    companion object {
        fun newDialog(title: String, message: String, positive: String, negative: String, tag: String): QuestionDialog {
            val f = QuestionDialog()
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

    private fun setResult(resultVal: Boolean) {
        val dialogResult = Bundle()
        dialogResult.putBoolean("QuestionResult", resultVal)

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
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(dialogPositive) { _, _ -> setResult(true) }
        builder.setNegativeButton(dialogNegative) { _, _ -> setResult(false) }
        return builder.create()
    }
}