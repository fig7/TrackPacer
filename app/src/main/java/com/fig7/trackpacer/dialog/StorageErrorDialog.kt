package com.fig7.trackpacer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.R
import kotlin.system.exitProcess

class StorageErrorDialog: DialogFragment() {

    companion object {
        fun newDialog(action: String, actionFatal: Boolean): StorageErrorDialog {
            val f = StorageErrorDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("action", action)
            args.putBoolean("fatality", actionFatal)
            f.arguments = args

            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()

        val action      = args.getString("action").toString()
        val actionFatal = args.getBoolean("fatality")

        val title   = "Loading error"
        val message = "An error occurred while $action distances and times. Please try the operation again. If that doesn't work, re-install the application."

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> if (actionFatal) exitProcess(-1) }
            .create()
    }
}