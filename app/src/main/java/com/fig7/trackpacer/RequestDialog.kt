package com.fig7.trackpacer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class RequestDialog: DialogFragment() {
    private lateinit var dialogTitle: String
    private lateinit var dialogMessage: String
    private lateinit var dialogTag: String
    private var dialogResult = Bundle()

    companion object {
        fun newDialog(title: String, message: String, tag: String): RequestDialog {
            val f = RequestDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("title",   title)
            args.putString("message", message)
            args.putString("tag",     tag)
            f.arguments = args
            return f
        }
    }

    private fun setResult(resultVal: Boolean) {
        dialogResult.putBoolean("RequestResult", resultVal)
        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogTitle   = arguments?.getString("title").toString()
        dialogMessage = arguments?.getString("message").toString()
        dialogTag     = arguments?.getString("tag").toString()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(R.string.request_permission) { _, _ -> setResult(true)}
        builder.setNegativeButton(R.string.cancel) { _, _ -> setResult(false) }
        return builder.create()
    }
}