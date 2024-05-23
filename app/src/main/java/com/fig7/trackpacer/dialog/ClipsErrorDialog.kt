package com.fig7.trackpacer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.R
import com.fig7.trackpacer.util.Bool
import kotlin.system.exitProcess

class ClipsErrorDialog: DialogFragment() {

    companion object {
        fun newDialog(action: String, actionFatal: Bool): ClipsErrorDialog {
            val f = ClipsErrorDialog()
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
        val message = "An error occurred while $action the clip library. Please try the operation again. If that doesn't work, re-install the application."

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> if (actionFatal) exitProcess(-1) }
            .create()
    }
}