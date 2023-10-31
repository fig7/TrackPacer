package com.fig7.trackpacer

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import com.fig7.trackpacer.data.ResultData
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.ActivityPastBinding


class PastActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPastBinding

    private val resultModel: ResultModel by viewModels()
    private val statusModel: StatusModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.extras == null) { throw AssertionError("PastActivity::onCreate(): extras is null") }

        binding = ActivityPastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        statusModel.startDelay = initData.getString("StartDelay")!!
        statusModel.powerStart = initData.getBoolean("PowerStart")
        statusModel.quickStart = initData.getBoolean("QuickStart")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            initData.classLoader   = ResultData::class.java.classLoader
            resultModel.resultData = initData.getParcelable("ResultParcel", ResultData::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            resultModel.resultData = initData.getParcelable("ResultParcel")!!
        }
    }
}
