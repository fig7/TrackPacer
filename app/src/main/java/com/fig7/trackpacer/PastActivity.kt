package com.fig7.trackpacer

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fig7.trackpacer.data.ResultData
import com.fig7.trackpacer.data.ResultModel
import com.fig7.trackpacer.databinding.ActivityPastBinding


class PastActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPastBinding
    private val resultModel: ResultModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.extras == null) { throw AssertionError("PastActivity::onCreate(): extras is null") }

        binding = ActivityPastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initData = intent.extras!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resultModel.resultData = initData.getParcelable("resultParcel", ResultData::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            resultModel.resultData = initData.getParcelable("resultParcel")!!
        }
    }
}