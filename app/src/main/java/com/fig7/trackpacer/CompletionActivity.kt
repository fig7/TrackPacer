package com.fig7.trackpacer

import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import com.fig7.trackpacer.dialog.InfoDialog
import com.fig7.trackpacer.data.HistoryModel
import com.fig7.trackpacer.data.ResultData
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.ActivityCompletionBinding


class CompletionActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCompletionBinding

    private val resultModel:  ResultModel  by viewModels()
    private val historyModel: HistoryModel by viewModels()
    private val statusModel:  StatusModel  by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.extras == null) { throw AssertionError("CompletionActivity::onCreate(): extras is null") }

        binding = ActivityCompletionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        statusModel.startDelay = initData.getString("StartDelay")!!
        statusModel.powerStart = initData.getBoolean("PowerStart")
        statusModel.quickStart = initData.getBoolean("QuickStart")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            initData.classLoader   = ResultData::class.java.classLoader
            resultModel.resultData = BundleCompat.getParcelable(initData,"resultParcel", ResultData::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            resultModel.resultData = initData.getParcelable("resultParcel")!!
        }

        supportFragmentManager.setFragmentResultListener("SAVE_ME", this) { _: String, resultBundle: Bundle ->
            val resultData: ResultData

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                resultData = resultBundle.getParcelable("resultParcel", ResultData::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                resultData = resultBundle.getParcelable("resultParcel")!!
            }

            val historyManager = historyModel.historyManager
            if(!historyManager.saveHistory(resultData)) {
                val dialog = InfoDialog.newDialog("Error saving result",
                "An error occurred while saving the pacing result." +
                        "The result was not saved. Please try saving again.")

                dialog.show(supportFragmentManager, "HISTORY_SAVING_DIALOG")
                return@setFragmentResultListener
            }

            finish()
        }

        supportFragmentManager.setFragmentResultListener("CLOSE_ME", this) { _: String, _: Bundle ->
            finish()
        }

        // Ignore back button presses (we want the user to explicitly choose an option)
        onBackPressedDispatcher.addCallback(this) { }
    }
}