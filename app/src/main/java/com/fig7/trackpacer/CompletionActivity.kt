package com.fig7.trackpacer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.databinding.ActivityCompletionBinding
import java.lang.AssertionError

class CompletionActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCompletionBinding

    private val resultModel: ResultModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.extras == null) { throw AssertionError("CompletionActivity::onCreate(): extras is null") }

        binding = ActivityCompletionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        resultModel.startTime    = initData.getLong("StartTime")
        resultModel.runDist      = initData.getString("RunDist")!!
        resultModel.runLane      = initData.getInt("RunLane")
        resultModel.runProf      = initData.getString("RunProf")!!
        resultModel.totalDistStr = initData.getString("TotalDistStr")!!
        resultModel.totalTimeStr = initData.getString("TotalTimeStr")!!
        resultModel.totalPaceStr = initData.getString("TotalPaceStr")!!
        resultModel.actualTimeStr = initData.getString("ActualTimeStr")!!
        resultModel.actualPaceStr = initData.getString("ActualPaceStr")!!
        resultModel.earlyLateStr  = initData.getString("EarlyLateStr")!!

        supportFragmentManager.setFragmentResultListener("CLOSE_ME", this) { _: String, _: Bundle ->
            finish()
        }

        // Save state (via HistoryModel)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }
}