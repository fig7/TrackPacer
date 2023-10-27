package com.fig7.trackpacer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if ((context == null) || intent == null) { return }

        val intentAction = intent.action
        if (intentAction == Intent.ACTION_SCREEN_OFF) {
            context.sendBroadcast(Intent("TrackPacer.POWER_BEGIN_PACING"))
        } else if (intentAction == Intent.ACTION_SCREEN_ON) {
            context.sendBroadcast(Intent("TrackPacer.POWER_PAUSE_PACING"))
        }
    }
}
