package com.fig7.trackpacer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fig7.trackpacer.PacingStatus

class PacingModel: ViewModel() {
    var runDist = ""
    var runLaps = ""
    var runProf = ""
    var runLane = -1
    var runTime = -1.0

    var pausedTime = -1L

    // pacingStatus needs to be live, so it can be observed
    var pacingStatus = PacingStatus.NotPacing
    // From PacingCancel to NotPacing
    // goButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
    // goButton.isEnabled = true
    // goButton.isClickable = true
}