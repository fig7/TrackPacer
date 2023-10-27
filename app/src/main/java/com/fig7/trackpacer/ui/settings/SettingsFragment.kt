package com.fig7.trackpacer.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.SettingsModel
import com.fig7.trackpacer.databinding.FragmentSettingsBinding
import com.fig7.trackpacer.dialog.InfoDialog

class SettingsFragment: Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private val settingsModel: SettingsModel by activityViewModels()

    private fun setPowerStart(powerStart: Boolean) {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setPowerStart(powerStart)) {
            handleSettingsError()
        }
    }

    private fun setAlternateStart(alternateStart: Boolean) {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setAlternateStart(alternateStart)) {
            handleSettingsError()
        }
    }

    private fun handleSettingsError() {
        val dialog = InfoDialog.newDialog("Error saving settings",
            "An error occurred while saving the settings data." +
            "The changes were not saved. Please try again. If that doesn't work, re-start the app and try again.")

        dialog.show(parentFragmentManager, "SETTINGS_SAVING_DIALOG")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val settingsView = binding!!

        val settingsList    = settingsView.settingsList
        val settingsManager = settingsModel.settingsManager
        val settingsData    = settingsManager.settingsData

        settingsList.setContent {
            Column (modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)) {
                Divider(color = Color.Black)

                LazyColumn {
                    item {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                            .padding(horizontal = 1.dp, vertical = 16.dp)
                            .fillMaxWidth()) {

                            Column {
                                Text(
                                    text = "Start delay", fontSize = 16.sp, fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Seconds to delay start by", fontSize = 14.sp, textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            }

                            TextField(value = settingsData.startDelay.value, onValueChange = { })
                        }

                        Divider(color = Color.Black)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                            .padding(horizontal = 1.dp, vertical = 16.dp)
                            .fillMaxWidth()) {

                            Column {
                                Text(
                                    text = "Power start", fontSize = 16.sp, fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Use the power button to start and pause", fontSize = 14.sp, textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            }

                            Switch(checked = settingsData.powerStart.value, onCheckedChange = { setPowerStart(it) })
                        }

                        Divider(color = Color.Black)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                            .padding(horizontal = 1.dp, vertical = 16.dp)
                            .fillMaxWidth()) {

                            Column {
                                Text(
                                    text = "Alternate start", fontSize = 16.sp, fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Swap start and finish for 1, 3, and 5km", fontSize = 14.sp, textAlign = TextAlign.Center,
                                    modifier = Modifier
                                )
                            }

                            Switch(checked = settingsData.alternateStart.value, onCheckedChange = { setAlternateStart(it) })
                        }

                        Divider(color = Color.Black)
                    }
                }
            }
        }

        return settingsView.root
    }

    override fun onResume() {
        super.onResume()

        val settingsView = binding!!
        val context = requireContext()

        val phoneIcon = settingsView.settingsPhoneStatus
        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = settingsView.settingsDelaySetting
        delaySetting.setText(R.string.start_delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}