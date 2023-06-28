package com.fig7.trackpacer

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper

val clipList = arrayOf(
    R.raw.fifty, R.raw.onehundred, R.raw.onehundredandfifty, R.raw.twohundred,
    R.raw.twohundredandfifty, R.raw.threehundred, R.raw.threehundredandfifty, R.raw.fourhundred)

const val goClip = R.raw.threetwoone
const val finishClip = R.raw.finish
const val goClipOffset = 2000L

class WaypointService : Service() {
    private lateinit var mNM: NotificationManager

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        handleWaypoint()
    }

    private lateinit var mpStart: MediaPlayer
    private lateinit var mpFinish: MediaPlayer
    private lateinit var mpWaypoint: Array<MediaPlayer>
    private val waypointCalculator = WaypointCalculator()

    override fun onCreate() {
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel("TrackPacker_NC", "TrackPacer", NotificationManager.IMPORTANCE_LOW)
        channel.description = "TrackPacer notifications"
        mNM.createNotificationChannel(channel)

        mpStart  = MediaPlayer.create(this, goClip)
        mpFinish = MediaPlayer.create(this, finishClip)
        mpWaypoint = Array(8) { i -> MediaPlayer.create(this, clipList[i]) }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = Notification.Builder(this, "TrackPacker_NC")
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_pacing))
            .setSmallIcon(R.drawable.play)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)


        val runTimeStr = intent.getStringExtra("time")!!.split(":")
        val runTime = runTimeStr[0].trim().toInt()*60.0 + runTimeStr[1].toInt()

        waypointCalculator.initRun(runTime*1000.0)
        mpStart.setOnCompletionListener {
            val nextTime = waypointCalculator.beginRun()
            handler.postDelayed(runnable, nextTime.toLong() - goClipOffset)
        }

        mpStart.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mpStart.release()
        mpFinish.release()
        for (mp in mpWaypoint) mp.release()

        mNM.cancel(1)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun handleWaypoint() {
        if (waypointCalculator.waypointsRemaining()) {
            val i = waypointCalculator.waypointNum() % 8
            mpWaypoint[i].start()

            val nextTime = waypointCalculator.nextWaypointIn()
            handler.postDelayed(runnable, nextTime.toLong())
        } else {
            mpFinish.setOnCompletionListener { stopSelf() }
            mpFinish.start()
        }
    }
}