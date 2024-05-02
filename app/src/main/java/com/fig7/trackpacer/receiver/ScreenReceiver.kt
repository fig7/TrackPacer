package com.fig7.trackpacer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if((context == null) || intent == null) { return }

        val tpAction = when(intent.action) {
            Intent.ACTION_SCREEN_OFF -> "TrackPacer.POWER_BEGIN_PACING"
            Intent.ACTION_SCREEN_ON  -> "TrackPacer.POWER_PAUSE_PACING"
            else                     -> return
        }

        val tpIntent = Intent(tpAction)
        tpIntent.setPackage(context.packageName)
        context.sendBroadcast(tpIntent)
    }
}
