package com.fig7.trackpacer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ActivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) { return }

        val activity = context as MainActivity
        activity.handleIncomingCall()
    }
}