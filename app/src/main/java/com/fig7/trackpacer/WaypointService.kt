package com.fig7.trackpacer

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper

val clipList = arrayOf(
    R.raw.fifty, R.raw.onehundred, R.raw.onehundredandfifty, R.raw.twohundred,
    R.raw.twohundredandfifty, R.raw.threehundred, R.raw.threehundredandfifty, R.raw.fourhundred,
    R.raw.lap2, R.raw.lap3, R.raw.lap4, R.raw.lap5, R.raw.lap6, R.raw.lap7, R.raw.lap8, R.raw.lap9, R.raw.lap10, R.raw.lap11, R.raw.lap12, R.raw.lap13)

val clipMap = mapOf(
    "400m"   to arrayOf(0, 1, 2, 3, 4, 5 ,6, 7),
    "800m"   to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  7),
    "1200m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 7),
    "1500m"  to arrayOf(      2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5, 6,  7),
    "3000m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5 ,6, 11,
                        0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14, 0, 1, 2, 3),
    "5000m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5, 6, 11,
                        0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14, 0, 1, 2, 3, 4, 5, 6, 15,
                        0, 1, 2, 3, 4, 5, 6, 16, 0, 1, 2, 3, 4, 5, 6, 17, 0, 1, 2, 3, 4, 5, 6, 18, 0, 1, 2, 3, 4, 5, 6, 19, 0, 1, 2, 3),
    "1 mile" to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5, 6, 7))

const val goClip = R.raw.threetwoone
const val finishClip = R.raw.finish
const val goClipOffset = 1000L

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
    private var clipIndexList: Array<Int> = emptyArray()

    override fun onCreate() {
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel("TrackPacer_NC", "TrackPacer", NotificationManager.IMPORTANCE_LOW)
        channel.description = "TrackPacer notifications"
        mNM.createNotificationChannel(channel)

        mpStart  = MediaPlayer.create(this, goClip)
        mpFinish = MediaPlayer.create(this, finishClip)
        mpWaypoint = Array(clipList.size) { i -> MediaPlayer.create(this, clipList[i]) }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = Notification.Builder(this, "TrackPacer_NC")
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_pacing))
            .setSmallIcon(R.drawable.play)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        val runDist    = intent.getStringExtra("dist")!!
        val runTimeStr = intent.getStringExtra("time")!!.split(":")
        val runTime    = runTimeStr[0].trim().toInt()*60.0 + runTimeStr[1].toInt()

        clipIndexList = clipMap[runDist]!!
        waypointCalculator.initRun(runDist, runTime*1000.0)

        mpStart.setOnCompletionListener {
            val nextTime = waypointCalculator.beginRun()
            handler.postDelayed(runnable, nextTime.toLong() - goClipOffset)
        }

        mpStart.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)

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
            val i = clipIndexList[waypointCalculator.waypointNum()]
            mpWaypoint[i].start()

            val nextTime = waypointCalculator.nextWaypointIn()
            handler.postDelayed(runnable, nextTime.toLong())
        } else {
            mpFinish.setOnCompletionListener { stopSelf() }
            mpFinish.start()
        }
    }
}