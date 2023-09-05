package com.fig7.trackpacer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.fig7.trackpacer.data.PacingModel
import com.fig7.trackpacer.databinding.ActivityCompletionBinding

class CompletionActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCompletionBinding

    private lateinit var runDist: String
    private lateinit var runProf: String
    private var runLane = -1
    private var runTime = -1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompletionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        runDist = initData.getString("RunDist")!!
        runProf = initData.getString("RunProf")!!
        runLane = initData.getInt("RunLane")
        runTime = initData.getDouble("RunTime")

        // Display results (via CompletionViewModel)
        // Grab buttons and act on them
        supportFragmentManager.setFragmentResultListener("CLOSE_ME", this) { _: String, _: Bundle ->
            finish()
        }

        // Save state (via HistoryModel)
    }
}