package com.fig7.trackpacer

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner

val clipList = arrayOf(
    R.raw.fifty, R.raw.onehundred, R.raw.onehundredandfifty, R.raw.twohundred,
    R.raw.twohundredandfifty, R.raw.threehundred, R.raw.threehundredandfifty, R.raw.fourhundred)

const val goClip = R.raw.threetwoone
const val finishClip = R.raw.finish
const val goClipOffset = 2000L

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val waypointCalculator = WaypointCalculator()
    private lateinit var mpStart: MediaPlayer
    private lateinit var mpFinish: MediaPlayer
    private lateinit var mpWaypoint: Array<MediaPlayer>

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        handleWaypoint()
    }

    private fun handleWaypoint() {
        if (waypointCalculator.waypointsRemaining()) {
            val i = waypointCalculator.waypointNum() % 8
            mpWaypoint[i].start()

            val nextTime = waypointCalculator.nextWaypointIn()
            handler.postDelayed(runnable, nextTime.toLong())
        } else {
            mpFinish.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mpStart  = MediaPlayer.create(this, goClip)
        mpFinish = MediaPlayer.create(this, finishClip)
        mpWaypoint = Array(8) { i -> MediaPlayer.create(this, clipList[i]) }

        val spinner1 = findViewById<Spinner>(R.id.spinner_distance)
        ArrayAdapter.createFromResource(this,
            R.array.distance_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner1.adapter = adapter
        }

        val spinner2 = findViewById<Spinner>(R.id.spinner_time)
        ArrayAdapter.createFromResource(this,
            R.array.time_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }

        val button = findViewById<ImageButton>(R.id.button_go)
        button.setOnClickListener {
            mpStart.setOnCompletionListener {
                val nextTime = waypointCalculator.beginRun()
                handler.postDelayed(runnable, nextTime.toLong() - goClipOffset)
            }

            mpStart.start()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }
}