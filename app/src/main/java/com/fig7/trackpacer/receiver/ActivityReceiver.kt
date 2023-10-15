package com.fig7.trackpacer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fig7.trackpacer.PacingActivity

class ActivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) { return }

        val activity = context as PacingActivity
        activity.handleIncomingCall()
    }
}