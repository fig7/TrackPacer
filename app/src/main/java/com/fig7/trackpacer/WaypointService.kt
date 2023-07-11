package com.fig7.trackpacer

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.media.MediaPlayer
import android.os.*
import android.os.PowerManager.WakeLock

val clipList = arrayOf(
    R.raw.fifty, R.raw.onehundred, R.raw.onehundredandfifty, R.raw.twohundred,
    R.raw.twohundredandfifty, R.raw.threehundred, R.raw.threehundredandfifty, R.raw.fourhundred,
    R.raw.lap2,  R.raw.lap3,  R.raw.lap4,  R.raw.lap5,  R.raw.lap6,  R.raw.lap7,  R.raw.lap8,  R.raw.lap9,  R.raw.lap10, R.raw.lap11, R.raw.lap12, R.raw.lap13,
    R.raw.lap14, R.raw.lap15, R.raw.lap16, R.raw.lap17, R.raw.lap18, R.raw.lap19, R.raw.lap20, R.raw.lap21, R.raw.lap22, R.raw.lap23, R.raw.lap24, R.raw.lap25)

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
    "10000m" to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5, 6, 11,
                        0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14, 0, 1, 2, 3, 4, 5, 6, 15,
                        0, 1, 2, 3, 4, 5, 6, 16, 0, 1, 2, 3, 4, 5, 6, 17, 0, 1, 2, 3, 4, 5, 6, 18, 0, 1, 2, 3, 4, 5, 6, 19,
                        0, 1, 2, 3, 4, 5, 6, 20, 0, 1, 2, 3, 4, 5, 6, 21, 0, 1, 2, 3, 4, 5, 6, 22, 0, 1, 2, 3, 4, 5, 6, 23,
                        0, 1, 2, 3, 4, 5, 6, 24, 0, 1, 2, 3, 4, 5, 6, 25, 0, 1, 2, 3, 4, 5, 6, 26, 0, 1, 2, 3, 4, 5, 6, 27,
                        0, 1, 2, 3, 4, 5, 6, 28, 0, 1, 2, 3, 4, 5, 6, 29, 0, 1, 2, 3, 4, 5, 6, 30, 0, 1, 2, 3, 4, 5, 6, 31, 0, 1, 2, 3, 4, 5, 6, 7),

    "1 mile" to arrayOf(0, 1, 2, 3, 4, 5 ,6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5, 6, 7))

private const val goClip = R.raw.threetwoone
private const val finishClip = R.raw.finish

private const val goStartOffset = 5000L
private const val goClipOffset  = 2000L

class WaypointService : Service() {
    private val wsBinder = LocalBinder()
    private var startTime = -1L

    private lateinit var mNM: NotificationManager
    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var wakeLock: WakeLock

    private val handler = Handler(Looper.getMainLooper())
    private val waypointRunnable = Runnable {
        handleWaypoint()
    }

    private val startRunnable = Runnable {
        mpStart.start()
    }

    private lateinit var mpStart: MediaPlayer
    private lateinit var mpFinish: MediaPlayer
    private lateinit var mpWaypoint: Array<MediaPlayer>

    private val waypointCalculator = WaypointCalculator()
    private lateinit var clipIndexList: Array<Int>

    inner class LocalBinder : Binder() {
        fun getService(): WaypointService = this@WaypointService
    }

    fun beginPacing(runDistStr: String, runTimeStr: String): Boolean {
        val runTimeSplit = runTimeStr.split(":")
        val runTime      = runTimeSplit[0].trim().toInt()*60.0 + runTimeSplit[1].toInt()

        clipIndexList = clipMap[runDistStr]!!
        waypointCalculator.initRun(runDistStr, runTime*1000.0)

        val res = audioManager.requestAudioFocus(focusRequest)
        return if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            startTime = SystemClock.elapsedRealtime() + goStartOffset
            handler.postDelayed(startRunnable, goClipOffset)
            true
        } else {
            stopSelf()
            false
        }
    }

    fun elapsedTime() : Long {
        return SystemClock.elapsedRealtime() - startTime
    }

    override fun onCreate() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackPacer::WaypointServiceWakeLock")
        wakeLock.acquire()

        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("TrackPacer_NC", "TrackPacer", NotificationManager.IMPORTANCE_LOW)
        channel.description = "TrackPacer notifications"
        mNM.createNotificationChannel(channel)

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = Notification.Builder(this, "TrackPacer_NC")
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_pacing))
            .setSmallIcon(R.drawable.play_small)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        mpStart  = MediaPlayer.create(this, goClip)
        mpFinish = MediaPlayer.create(this, finishClip)
        mpFinish.setOnCompletionListener { wakeLock.release() }

        mpWaypoint = Array(clipList.size) { i -> MediaPlayer.create(this, clipList[i]) }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                setFocusGain(AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                build()
            })
            build()
        }

        mpStart.setOnCompletionListener {
            audioManager.abandonAudioFocusRequest(focusRequest)

            val nextTime = waypointCalculator.beginRun()
            handler.postDelayed(waypointRunnable, nextTime.toLong())
        }

        for(mp in mpWaypoint) mp.setOnCompletionListener { audioManager.abandonAudioFocusRequest(focusRequest) }
        mpFinish.setOnCompletionListener { audioManager.abandonAudioFocusRequest(focusRequest) }
    }

    override fun onDestroy() {
        handler.removeCallbacks(startRunnable)
        handler.removeCallbacks(waypointRunnable)

        mpStart.release()
        mpFinish.release()
        for (mp in mpWaypoint) mp.release()

        audioManager.abandonAudioFocusRequest(focusRequest)
        mNM.cancel(1)

        wakeLock.release()
    }

    override fun onBind(intent: Intent?): IBinder {
        return wsBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)

        stopSelf()
        return false
    }

    private fun handleWaypoint() {
        if (waypointCalculator.waypointsRemaining()) {
            val i = clipIndexList[waypointCalculator.waypointNum()]
            val res = audioManager.requestAudioFocus(focusRequest)
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mpWaypoint[i].start()
            }

            val nextTime = waypointCalculator.nextWaypoint()
            handler.postDelayed(waypointRunnable, nextTime.toLong()-elapsedTime())
        } else {
            val res = audioManager.requestAudioFocus(focusRequest)
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mpFinish.start()
            }
        }
    }
}
