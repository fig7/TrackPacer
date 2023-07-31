package com.fig7.trackpacer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if ((context == null) || intent == null) { return }
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) { return }

        val extras = intent.extras ?: return
        val state  = extras.getString("state") ?: return

        if (state == "RINGING") {
            context.sendBroadcast(Intent("TrackPacer.PAUSE_PACING"))
        }
    }
}