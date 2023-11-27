package com.fig7.trackpacer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.R

class RequestDialog: DialogFragment() {
    private lateinit var dialogTitle: String
    private lateinit var dialogMessage: String
    private lateinit var dialogTag: String

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
        val dialogResult = Bundle()
        dialogResult.putBoolean("RequestResult", resultVal)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()

        dialogTitle   = args.getString("title").toString()
        dialogMessage = args.getString("message").toString()
        dialogTag     = args.getString("tag").toString()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(R.string.request_continue) { _, _ -> setResult(true) }
        builder.setNegativeButton(R.string.request_cancel)   { _, _ -> setResult(false) }
        return builder.create()
    }
}