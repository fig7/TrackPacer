package com.fig7.trackpacer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlin.system.exitProcess

class DataErrorDialog: DialogFragment() {

    companion object {
        fun newDialog(action: String, actionFatal: Boolean): DataErrorDialog {
            val f = DataErrorDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("action", action)
            args.putBoolean("fatality", actionFatal)
            f.arguments = args

            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val action      = arguments?.getString("action").toString()
        val actionFatal = arguments?.getBoolean("fatality")

        val title   = "Data error"
        val message = "An error occurred while $action distances and times. Please try the operation again. If that doesn't work, re-install the application."

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _,_ -> if (actionFatal == true) exitProcess(-1) }
            .create()
    }
}