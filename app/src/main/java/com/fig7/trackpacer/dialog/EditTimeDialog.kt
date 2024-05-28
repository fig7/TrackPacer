package com.fig7.trackpacer.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.fig7.trackpacer.enums.EditResult
import com.fig7.trackpacer.R
import java.util.Locale

class EditTimeDialog: DialogFragment() {
    private lateinit var dialogTime: String
    private lateinit var dialogTimeArray: Array<String>
    private lateinit var dialogTag: String

    private lateinit var deleteButton: Button
    private lateinit var addButton: Button
    private lateinit var setButton: Button

    private var mins = 0
    private var secs = 0
    private var hths = 0

    companion object {
        fun newDialog(time: String, timeArray:Array<String>, tag: String): EditTimeDialog {
            val f = EditTimeDialog()
            f.isCancelable = false

            val args = Bundle()
            args.putString("time",       time)
            args.putStringArray("times", timeArray)
            args.putString("tag",        tag)
            f.arguments = args

            return f
        }
    }

    private fun runTimeFromString(runTimeStr: String): Double {
        val runTimeSplit = runTimeStr.split(":")
        return 1000.0*(runTimeSplit[0].trim().toLong()*60.0 + runTimeSplit[1].toDouble())
    }

    private fun stringFromRunTime(mins: Int, secs: Int, hths: Int): String {
        return getString(R.string.edit_time_all, mins.toString(), String.format(Locale.ROOT, "%02d", secs), String.format(Locale.ROOT, "%02d", hths))
    }

    private fun updateButtons() {
        val runTimeStr = stringFromRunTime(mins, secs, hths)
        addButton.text = getString(R.string.add_time, runTimeStr)
        setButton.text = getString(R.string.set_time, runTimeStr)

        // TODO: This should really use a waypoint check (min 5s between waypoints)
        if ((runTimeStr == dialogTime) || ((mins == 0) && (secs<40))) {
            addButton.isEnabled   = false
            addButton.isClickable = false

            setButton.isEnabled   = false
            setButton.isClickable = false
            return
        }

        val timeDuplicated = dialogTimeArray.contains(runTimeStr)
        setButton.isEnabled   = !timeDuplicated
        setButton.isClickable = !timeDuplicated

        addButton.isEnabled   = !timeDuplicated
        addButton.isClickable = !timeDuplicated
    }

    private fun cancelEdit() {
        val dialogResult = Bundle()
        dialogResult.putInt("EditResult", EditResult.Cancel.ordinal)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    private fun deleteEdit() {
        val dialogResult = Bundle()
        dialogResult.putString("EditTime", dialogTime)
        dialogResult.putInt("EditResult", EditResult.Delete.ordinal)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    private fun addEdit() {
        val runTimeStr = stringFromRunTime(mins, secs, hths)

        val dialogResult = Bundle()
        dialogResult.putString("EditTime", runTimeStr)
        dialogResult.putInt("EditResult", EditResult.Add.ordinal)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    private fun setEdit() {
        val runTimeStr = stringFromRunTime(mins, secs, hths)

        val dialogResult = Bundle()
        dialogResult.putString("OrigTime", dialogTime)
        dialogResult.putString("EditTime", runTimeStr)
        dialogResult.putInt("EditResult", EditResult.Set.ordinal)

        parentFragmentManager.setFragmentResult(dialogTag, dialogResult)
        dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()

        dialogTime      = args.getString("time").toString()
        dialogTimeArray = args.getStringArray("times") as Array<String>
        dialogTag       = args.getString("tag").toString()

        val v: View = inflater.inflate(R.layout.dialog_edit_time, container, false)
        val titleLabel = v.findViewById<TextView>(R.id.edit_time_title)
        titleLabel.text = getString(R.string.edit_time_title, dialogTime)

        val cancelButton = v.findViewById<ImageButton>(R.id.edit_time_cancel)
        cancelButton.setOnClickListener { cancelEdit() }

        val runTime = runTimeFromString(dialogTime)
        mins = (runTime / 60000.0).toInt()
        secs = ((runTime - mins*60000.0) / 1000.0).toInt()
        hths = ((runTime - mins*60000.0 - secs*1000.0)/10.0).toInt()

        val minPicker = v.findViewById<NumberPicker>(R.id.min_picker)
        minPicker.setOnValueChangedListener { _, _, newVal ->
            mins = newVal
            updateButtons()
        }
        minPicker.minValue = 0
        minPicker.maxValue = 99
        minPicker.value = mins

        val secPicker = v.findViewById<NumberPicker>(R.id.sec_picker)
        secPicker.setOnValueChangedListener { _, _, newVal ->
            secs = newVal
            updateButtons()
        }
        secPicker.setFormatter { String.format(Locale.ROOT, "%02d", it); }
        secPicker.minValue = 0
        secPicker.maxValue = 59
        secPicker.value = secs

        val hthsPicker = v.findViewById<NumberPicker>(R.id.hths_picker)
        hthsPicker.setOnValueChangedListener { _, _, newVal ->
            hths = newVal
            updateButtons()
        }
        hthsPicker.setFormatter { String.format(Locale.ROOT, "%02d", it); }
        hthsPicker.minValue = 0
        hthsPicker.maxValue = 99
        hthsPicker.value = hths

        deleteButton = v.findViewById(R.id.edit_time_delete)
        deleteButton.isEnabled   = (dialogTimeArray.size != 1)
        deleteButton.isClickable = (dialogTimeArray.size != 1)
        deleteButton.setOnClickListener { deleteEdit() }

        addButton = v.findViewById(R.id.edit_time_add)
        addButton.isEnabled = false
        addButton.isClickable = false
        addButton.text = getString(R.string.add_time, dialogTime)
        addButton.setOnClickListener { addEdit() }

        setButton = v.findViewById(R.id.edit_time_set)
        setButton.isEnabled = false
        setButton.isClickable = false
        setButton.text = getString(R.string.set_time, dialogTime)
        setButton.setOnClickListener { setEdit() }

        return v
    }
}