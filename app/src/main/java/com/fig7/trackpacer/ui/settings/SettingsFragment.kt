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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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

private enum class Validity {
    Valid, Invalid, Uncertain
}

private fun startDelayValid(startDelay: String): Validity {
    val startSplit = startDelay.split(".")
    val splitCount = startSplit.count()

    if(splitCount == 1) {
        val startSecs = startSplit[0]
        if(startSecs.isEmpty()) { return Validity.Uncertain }
        if(startSecs.length > 2) { return Validity.Invalid }

        val startSecsI = startSecs.toIntOrNull() ?: return Validity.Invalid
        if((startSecsI < 1) || (startSecsI > 30)) return Validity.Invalid

        return Validity.Valid
    } else if(splitCount != 2) { return Validity.Invalid }

    val startSecs = startSplit[0]
    if(startSecs.isEmpty()) { return Validity.Uncertain }
    if(startSecs.length > 2) { return Validity.Invalid }

    val startSecsI = startSecs.toIntOrNull() ?: return Validity.Invalid
    if((startSecsI < 1) || (startSecsI > 30)) return Validity.Invalid

    val startHths = startSplit[1]
    if(startHths.isEmpty()) { return Validity.Uncertain }
    if(startHths.length > 2) { return Validity.Invalid }

    val startHthsI = startHths.toIntOrNull() ?: return Validity.Invalid
    if(startHthsI < 0) return Validity.Invalid

    if((startSecsI == 30) && (startHthsI != 0)) return Validity.Invalid
    return Validity.Valid
}

class SettingsFragment: Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private val settingsModel: SettingsModel by activityViewModels()

    class StartDelayTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            if(startDelayValid(text.toString()) == Validity.Valid) {
                return TransformedText(text, OffsetMapping.Identity)
            }

            val redText = AnnotatedString(text.toString(), listOf(AnnotatedString.Range(SpanStyle(color = Color.Red), 0, text.length)))
            return TransformedText(redText, OffsetMapping.Identity) }
        }

    private fun setStartDelay(newStartDelay: String): Boolean {
        var startDelay = newStartDelay
        val startSplit = startDelay.split(".")
        val splitCount = startSplit.count()

        if(splitCount == 1) {
            startDelay = "$startDelay.00"
        } else if(startSplit[1].length == 1) {
            startDelay += "0"
        }

        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setStartDelay(startDelay)) {
            handleSettingsError()
        }

        val settingsView = binding!!
        val delaySetting = settingsView.settingsDelaySetting
        delaySetting.text = settingsModel.settingsManager.settingsData.startDelay

        return true
    }

    private fun setPowerStart(newPowerStart: Boolean) {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setPowerStart(newPowerStart)) {
            handleSettingsError()
        }
    }

    private fun setAlternateStart(newAlternateStart: Boolean) {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setAlternateStart(newAlternateStart)) {
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
        val settingsList = settingsView.settingsList

        val settingsManager = settingsModel.settingsManager
        val settingsData    = settingsManager.settingsData

        settingsList.setContent {
            val focusManager = LocalFocusManager.current

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
                                Text(text = "Start delay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Seconds to delay the start by", fontSize = 14.sp, textAlign = TextAlign.Center)
                                Text(text = "(Enter a value between 1.00 and 30.00)", fontSize = 14.sp, textAlign = TextAlign.Center)
                            }

                            var startDelay by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text = settingsData.startDelay)) }
                            var startDelayFocus by remember { mutableStateOf(false) }
                            OutlinedTextField(value = startDelay,
                                onValueChange = {
                                    if(startDelayValid(it.text) != Validity.Invalid) { startDelay = it }
                                },

                                visualTransformation = StartDelayTransformation(), singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (startDelayValid(startDelay.text) == Validity.Valid) {
                                        focusManager.clearFocus()
                                    }
                                }),
                                modifier = Modifier
                                    .width(72.dp)
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            startDelayFocus = true
                                        } else if (startDelayFocus) {
                                            startDelayFocus = false
                                            if (startDelayValid(startDelay.text) == Validity.Valid) {
                                                setStartDelay(startDelay.text)
                                            }

                                            startDelay = startDelay.copy(text = settingsData.startDelay)
                                        }
                                    }
                            )
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

                            Switch(checked = settingsData.powerStart.value, onCheckedChange = { focusManager.clearFocus(); setPowerStart(it) })
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

                            Switch(checked = settingsData.alternateStart.value, onCheckedChange = { focusManager.clearFocus(); setAlternateStart(it) })
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
        delaySetting.text = String.format("%.2f", settingsModel.settingsManager.settingsData.startDelay.toDouble())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
