package com.fig7.trackpacer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fig7.trackpacer.PacingActivity

class PacingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if ((context == null) || (intent == null)) { return }

        val activity = context as PacingActivity
        when(intent.action) {
            "TrackPacer.CALL_PAUSE_PACING"  -> activity.handleIncomingIntent(begin = false, silent = true)
            "TrackPacer.POWER_PAUSE_PACING" -> activity.handleIncomingIntent(begin = false, silent = false)
            "TrackPacer.POWER_BEGIN_PACING" -> activity.handleIncomingIntent(begin = true,  silent = false)
        }
    }
}