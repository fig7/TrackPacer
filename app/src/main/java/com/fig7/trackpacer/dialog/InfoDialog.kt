package com.fig7.trackpacer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.R

class InfoDialog: DialogFragment() {
    private lateinit var dialogTitle: String
    private lateinit var dialogMessage: String

    companion object {
        fun newDialog(title: String, message: String): InfoDialog {
            val f = InfoDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("title", title)
            args.putString("message", message)
            f.arguments = args
            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogTitle = arguments?.getString("title").toString()
        dialogMessage = arguments?.getString("message").toString()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(R.string.ok) { _, _ -> }
        return builder.create()
    }
}