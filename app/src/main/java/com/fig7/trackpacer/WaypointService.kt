package com.fig7.trackpacer

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import android.os.*
import android.os.PowerManager.WakeLock
import kotlin.math.min

val clipList = arrayOf(
    R.raw.fifty, R.raw.onehundred, R.raw.onehundredandfifty, R.raw.twohundred,
    R.raw.twohundredandfifty, R.raw.threehundred, R.raw.threehundredandfifty,
    R.raw.lap2,  R.raw.lap3,  R.raw.lap4,  R.raw.lap5,  R.raw.lap6,  R.raw.lap7,  R.raw.lap8,  R.raw.lap9,  R.raw.lap10, R.raw.lap11, R.raw.lap12, R.raw.lap13,
    R.raw.lap14, R.raw.lap15, R.raw.lap16, R.raw.lap17, R.raw.lap18, R.raw.lap19, R.raw.lap20, R.raw.lap21, R.raw.lap22, R.raw.lap23, R.raw.lap24, R.raw.lap25, R.raw.finish)

var clipNames = arrayOf(
    "50m",    "100m",   "150m",   "200m",   "250m",   "300m",   "350m",
    "Lap 2",  "Lap 3",  "Lap 4",  "Lap 5",  "Lap 6",  "Lap 7",  "Lap 8",  "Lap 9",
    "Lap 10", "Lap 11", "Lap 12", "Lap 13", "Lap 14", "Lap 15", "Lap 16", "Lap 17",
    "Lap 18", "Lap 19", "Lap 20", "Lap 21", "Lap 22", "Lap 23", "Lap 24", "Lap 25", "Finish line")

val fL = clipList.size - 1
val clipMap = mapOf(
    "400m"   to arrayOf(0, 1, 2, 3, 4, 5 ,6, fL),
    "800m"   to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6, fL),
    "1200m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6, fL),
    "1500m"  to arrayOf(      0, 1, 2, 3 ,4,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, fL),
    "2000m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10, 0, 1, 2, 3, 4, 5, 6, fL),
    "3000m"  to arrayOf(            0, 1, 2,  7, 0, 1 ,2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10,
                        0, 1 ,2, 3, 4, 5, 6, 11, 0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, fL),
    "4000m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10,
                        0, 1, 2, 3, 4, 5, 6, 11, 0, 1, 2, 3, 4, 5 ,6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14,
                        0, 1, 2, 3, 4, 5, 6, 15, 0, 1, 2, 3, 4, 5, 6, fL),
    "5000m"  to arrayOf(            0, 1, 2,  7, 0, 1 ,2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10,
                        0, 1, 2, 3, 4, 5, 6, 11, 0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14,
                        0, 1, 2, 3, 4, 5, 6, 15, 0, 1, 2, 3, 4, 5, 6, 16, 0, 1, 2, 3, 4, 5, 6, 17, 0, 1, 2, 3, 4, 5, 6, 18, 0, 1, 2, 3, 4, 5, 6, fL),
    "10000m" to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10,
                        0, 1, 2, 3, 4, 5, 6, 11, 0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14,
                        0, 1, 2, 3, 4, 5, 6, 15, 0, 1, 2, 3, 4, 5, 6, 16, 0, 1, 2, 3, 4, 5, 6, 17, 0, 1, 2, 3, 4, 5, 6, 18,
                        0, 1, 2, 3, 4, 5, 6, 19, 0, 1, 2, 3, 4, 5, 6, 20, 0, 1, 2, 3, 4, 5, 6, 21, 0, 1, 2, 3, 4, 5, 6, 22,
                        0, 1, 2, 3, 4, 5, 6, 23, 0, 1, 2, 3, 4, 5, 6, 24, 0, 1, 2, 3, 4, 5, 6, 25, 0, 1, 2, 3, 4, 5, 6, 26,
                        0, 1, 2, 3, 4, 5, 6, 27, 0, 1, 2, 3, 4, 5, 6, 28, 0, 1, 2, 3, 4, 5, 6, 29, 0, 1, 2, 3, 4, 5, 6, 30, 0, 1, 2, 3, 4, 5, 6, fL),
    "1 mile" to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, fL))

// Alternative 3K and 5K (using main start, finish @200m)
// "3000m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5 ,6, 10,
//                     0, 1, 2, 3, 4, 5, 6, 11, 0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, fL),
// "5000m"  to arrayOf(0, 1, 2, 3, 4, 5 ,6,  7, 0, 1, 2, 3, 4, 5, 6,  8, 0, 1, 2, 3, 4, 5, 6,  9, 0, 1, 2, 3, 4, 5, 6, 10,
//                     0, 1, 2, 3, 4, 5, 6, 11, 0, 1, 2, 3, 4, 5, 6, 12, 0, 1, 2, 3, 4, 5, 6, 13, 0, 1, 2, 3, 4, 5, 6, 14,
//                     0, 1, 2, 3, 4, 5, 6, 15, 0, 1, 2, 3, 4, 5, 6, 16, 0, 1, 2, 3, 4, 5, 6, 17, 0, 1, 2, 3, 4, 5, 6, 18, 0, 1, 2, fL),



@Suppress("ConstPropertyName")
private const val GoStartOffset = 5000L

@Suppress("ConstPropertyName")
private const val GoClipOffset  = 2000L

class WaypointService : Service(), OnAudioFocusChangeListener {
    private val wsBinder = LocalBinder()

    private var startRealtime = -1L
    private var prevTime  = -1.0

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
    private lateinit var mpResume: MediaPlayer
    private lateinit var mpWaypoint: Array<MediaPlayer>

    private val waypointCalculator = WaypointCalculator()
    private lateinit var clipIndexList: Array<Int>

    inner class LocalBinder : Binder() {
        fun getService(): WaypointService = this@WaypointService
    }

    fun beginPacing(runDist: String, runTime: Double, runLane: Int): Boolean {
        clipIndexList = clipMap[runDist]!!
        waypointCalculator.initRun(runDist, runTime, runLane)

        val res = audioManager.requestAudioFocus(focusRequest)
        return if (res == AUDIOFOCUS_REQUEST_GRANTED) {
            prevTime = 0.0
            startRealtime = SystemClock.elapsedRealtime() + GoStartOffset

            handler.postDelayed(startRunnable, GoClipOffset)
            true
        } else {
            stopSelf()
            false
        }
    }

    fun resumePacing(runDist: String, runTime: Double, runLane: Int, resumeTime: Long): Boolean {
        clipIndexList = clipMap[runDist]!!
        prevTime = waypointCalculator.initResume(runDist, runTime, runLane, resumeTime.toDouble())

        val res = audioManager.requestAudioFocus(focusRequest)
        return if (res == AUDIOFOCUS_REQUEST_GRANTED) {
            startRealtime = SystemClock.elapsedRealtime() - resumeTime
            mpResume.start()
            true
        } else {
            stopSelf()
            false
        }
    }

    fun beginRun(elapsedTime: Long) {
        val nextTime   = waypointCalculator.waypointTime()
        val updateTime = (nextTime - elapsedTime.toDouble()).toLong()
        if (updateTime >= 0L)  handler.postDelayed(waypointRunnable, updateTime)
    }

    fun elapsedTime(): Long {
        return SystemClock.elapsedRealtime() - startRealtime
    }

    fun timeRemaining(elapsedTime: Long): Long {
        return waypointCalculator.runTime() - elapsedTime
    }

    fun waypointName(): String {
        val waypointNum = waypointCalculator.waypointNum()
        return clipNames[clipIndexList[waypointNum]]
    }

    fun waypointProgress(elapsedTime: Long): Double {
        val waypointTime = waypointCalculator.waypointTime()
        return min(1.0, (elapsedTime - prevTime) / (waypointTime - prevTime))
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackPacer:wsWakeLock")
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

        mpStart   = MediaPlayer.create(this, R.raw.threetwoone)
        mpResume  = MediaPlayer.create(this, R.raw.resumed)
        mpWaypoint = Array(clipList.size) { i -> MediaPlayer.create(this, clipList[i]) }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                setFocusGain(AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                build()
            })
            setOnAudioFocusChangeListener(this@WaypointService, handler)
            build()
        }

        mpStart.setOnCompletionListener { audioManager.abandonAudioFocusRequest(focusRequest) }

        for( i in 0..<fL ) mpWaypoint[i].setOnCompletionListener { audioManager.abandonAudioFocusRequest(focusRequest) }
        mpWaypoint[fL].setOnCompletionListener { audioManager.abandonAudioFocusRequest(focusRequest); wakeLock.release() }
    }

    override fun onDestroy() {
        handler.removeCallbacks(startRunnable)
        handler.removeCallbacks(waypointRunnable)

        mpStart.release()
        mpResume.release()
        for (mp in mpWaypoint) mp.release()

        audioManager.abandonAudioFocusRequest(focusRequest)
        mNM.cancel(1)

        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return wsBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)

        stopSelf()
        return false
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if ((focusChange == AUDIOFOCUS_LOSS) || (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) || (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)) {
            if (mpStart.isPlaying)  { mpStart.stop(); mpStart.prepare() }
            if (mpResume.isPlaying) { mpResume.stop(); mpResume.prepare() }
            for (mp in mpWaypoint) { if (mp.isPlaying) { mp.stop(); mp.prepare() } }

            audioManager.abandonAudioFocusRequest(focusRequest)
            handler.removeCallbacks(startRunnable)
        }
    }

    private fun handleWaypoint() {
        val i = clipIndexList[waypointCalculator.waypointNum()]
        val res = audioManager.requestAudioFocus(focusRequest)
        if (res == AUDIOFOCUS_REQUEST_GRANTED) {
            mpResume.stop()
            mpWaypoint[i].start()
        }

        if (waypointCalculator.waypointsRemaining()) {
            prevTime = waypointCalculator.waypointTime()
            val waypointTime = waypointCalculator.nextWaypoint()
            handler.postDelayed(waypointRunnable, waypointTime.toLong()-elapsedTime())
        }
    }
}
