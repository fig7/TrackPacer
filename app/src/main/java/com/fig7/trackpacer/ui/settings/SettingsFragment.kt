package com.fig7.trackpacer.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
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
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.FragmentSettingsBinding
import com.fig7.trackpacer.dialog.InfoDialog
import com.fig7.trackpacer.ui.theme.TPTheme

private enum class Validity {
    // Definitely valid
    Valid,

    // Definitely not valid
    Invalid,

    // Can be made valid
    Uncertain
}

private fun startDelayValid(startDelay: String): Validity {
    val startSplit = startDelay.split(".")
    val splitCount = startSplit.count()
    if(splitCount > 2) { return Validity.Invalid }                                     // Only one separator is allowed

    val startSecs  = startSplit[0]
    val startSecsI = startSecs.toIntOrNull()
    if(startSecs.isEmpty())                              { return Validity.Uncertain } // Empty string can be made valid
    if(startSecsI == null)                               { return Validity.Invalid }   // Simply invalid
    if((startSecs[0] == '0') && (startSecs.length > 1))  { return Validity.Invalid }   // Don't allow leading zeros

    if((startSecsI < 5) || (startSecsI > 30)) {                                        // Only 5 <= secs <= 30 is always valid
        if(startSecs.length >= 2)                        { return Validity.Invalid }   // > 30 is invalid
        if(startSecsI > 3)                               { return Validity.Invalid }   // 3 < secs < 5 is invalid
        if(splitCount == 1)                              { return Validity.Uncertain } // Otherwise incomplete, or need to check dp
    } else if(splitCount == 1)                           { return Validity.Valid }     // Valid if no dp yet

    val startHths = startSplit[1]
    if(startHths.isEmpty())                              { return Validity.Uncertain } // Empty hundredths can be made valid
    if(startHths.length > 2)                             { return Validity.Invalid }   // Only allow up to hundredths, not thousandths

    val startHthsI = startHths.toIntOrNull() ?: return Validity.Invalid                // Simply invalid
    if(startHthsI < 0) { return Validity.Invalid }                                     // Hundredths cannot be <0

    val startDelayI = 100*startSecsI + startHthsI                                      // Finally, check value to determine validity
    return if(startDelayI > 3000) Validity.Invalid else if(startDelayI <= 300) Validity.Uncertain else if(startDelayI < 500) Validity.Invalid else Validity.Valid
}

class SettingsFragment: Fragment() {
    private var binding: FragmentSettingsBinding? = null

    private val settingsModel: SettingsModel by activityViewModels()
    private val statusModel:   StatusModel   by activityViewModels()

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
            return false
        }

        statusModel.startDelay = startDelay
        val powerStart = statusModel.powerStart
        val quickStart = statusModel.quickStart
        startDelay     = statusModel.startDelay

        val settingsView = binding!!
        val delaySetting = settingsView.settingsDelaySetting

        val delayText = if(quickStart) "QCK" else if (powerStart) "PWR" else startDelay
        delaySetting.text = delayText
        return true
    }

    private fun setPowerStart(newPowerStart: Boolean): Boolean {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setPowerStart(newPowerStart)) {
            handleSettingsError()
            return false
        }

        statusModel.powerStart = newPowerStart
        val powerStart = statusModel.powerStart
        val quickStart = statusModel.quickStart
        val startDelay = statusModel.startDelay

        val settingsView = binding!!
        val pacingIcon   = settingsView.settingsPacingStatus
        val delaySetting = settingsView.settingsDelaySetting

        val context = requireContext()
        val pacingIconId = if(powerStart) R.drawable.power_stop_small else R.drawable.stop_small
        pacingIcon.setImageDrawable(AppCompatResources.getDrawable(context, pacingIconId))

        val delayText = if(quickStart) "QCK" else if (powerStart) "PWR" else startDelay
        delaySetting.text = delayText
        return true
    }

    private fun setQuickStart(newQuickStart: Boolean): Boolean {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setQuickStart(newQuickStart)) {
            handleSettingsError()
            return false
        }

        statusModel.quickStart = newQuickStart
        val powerStart = statusModel.powerStart
        val quickStart = statusModel.quickStart
        val startDelay = statusModel.startDelay

        val settingsView = binding!!
        val delaySetting = settingsView.settingsDelaySetting

        val delayText = if(quickStart) "QCK" else if (powerStart) "PWR" else startDelay
        delaySetting.text = delayText
        return true
    }

    private fun setAlternateStart(newAlternateStart: Boolean): Boolean {
        val settingsManager = settingsModel.settingsManager
        if(!settingsManager.setAlternateStart(newAlternateStart)) {
            handleSettingsError()
            return false
        }

        return true
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
            TPTheme {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)) {

                    Divider(color = MaterialTheme.colors.onBackground)

                    LazyColumn {
                        item {
                            val focusRequester = FocusRequester()
                            Row(horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .clickable { focusRequester.requestFocus() }
                                    .padding(horizontal = 1.dp, vertical = 16.dp)
                                    .fillMaxWidth()) {

                                Column {
                                    Text(text = "Start delay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                                    Text(text = "Seconds to delay the start by", fontSize = 14.sp, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)
                                    Text(text = "(between 5.00 and 30.00)", fontSize = 14.sp, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)
                                }

                                var startDelay by remember { mutableStateOf(TextFieldValue(text = settingsData.startDelay)) }
                                var startDelayFocus by remember { mutableStateOf(false) }
                                OutlinedTextField(value = startDelay,
                                    onValueChange = {
                                        if(startDelayValid(it.text) != Validity.Invalid) {
                                            startDelay = it
                                        }
                                    },

                                    visualTransformation = StartDelayTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    keyboardActions = KeyboardActions(onDone = {
                                        if(startDelayValid(startDelay.text) == Validity.Valid) {
                                            focusManager.clearFocus()
                                        }
                                    }),

                                    colors = TextFieldDefaults.outlinedTextFieldColors(textColor = MaterialTheme.colors.onBackground),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                    modifier = Modifier
                                        .width(80.dp)
                                        .align(Alignment.CenterVertically)
                                        .focusRequester(focusRequester)
                                        .onFocusChanged {
                                            if(it.isFocused) {
                                                startDelayFocus = true
                                            } else if(startDelayFocus) {
                                                startDelayFocus = false
                                                if(startDelayValid(startDelay.text) == Validity.Valid) {
                                                    setStartDelay(startDelay.text)
                                                }

                                                startDelay = startDelay.copy(text = settingsData.startDelay)
                                            }
                                        })
                            }

                            Divider(color = MaterialTheme.colors.onBackground)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(role = Role.Switch,
                                        value = settingsData.powerStart.value,
                                        onValueChange = { focusManager.clearFocus(); setPowerStart(it) })
                                    .padding(horizontal = 1.dp, vertical = 16.dp)) {

                                Column {
                                    Text(text = "Power start", fontSize = 16.sp, color = MaterialTheme.colors.onBackground, fontWeight = FontWeight.Bold)
                                    Text(text = "Power button to start and pause", fontSize = 14.sp, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)
                                }

                                Switch(checked = settingsData.powerStart.value, onCheckedChange = null)
                            }

                            Divider(color = MaterialTheme.colors.onBackground)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(role = Role.Switch,
                                        value = settingsData.quickStart.value,
                                        onValueChange = { focusManager.clearFocus(); setQuickStart(it) })
                                    .padding(horizontal = 1.dp, vertical = 16.dp)) {

                                Column {
                                    Text(text = "Quick start", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                                    Text(text = "Start is not delayed, just \"Go!\"", fontSize = 14.sp, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)
                                }

                                Switch(checked = settingsData.quickStart.value, onCheckedChange = null)
                            }

                            Divider(color = MaterialTheme.colors.onBackground)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(role = Role.Switch,
                                        value = settingsData.alternateStart.value,
                                        onValueChange = { focusManager.clearFocus(); setAlternateStart(it) })
                                    .padding(horizontal = 1.dp, vertical = 16.dp)) {

                                Column {
                                    Text(text = "Alternate start", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                                    Text(text = "Swap start and finish for 1, 3, and 5km", fontSize = 14.sp, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)
                                }

                                Switch(checked = settingsData.alternateStart.value, onCheckedChange = null)
                            }

                            Divider(color = MaterialTheme.colors.onBackground)
                        }
                    }
                }
            }
        }

        return settingsView.root
    }

    override fun onResume() {
        super.onResume()

        val settingsView = binding!!
        val pacingIcon   = settingsView.settingsPacingStatus
        val phoneIcon    = settingsView.settingsPhoneStatus
        val delaySetting = settingsView.settingsDelaySetting

        val powerStart = statusModel.powerStart
        val quickStart = statusModel.quickStart
        val startDelay = statusModel.startDelay

        val context = requireContext()
        val pacingIconId = if(powerStart) R.drawable.power_stop_small else R.drawable.stop_small
        pacingIcon.setImageDrawable(AppCompatResources.getDrawable(context, pacingIconId))

        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        val phoneIconId = if(phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, phoneIconId))

        val delayText = if(quickStart) "QCK" else if (powerStart) "PWR" else startDelay
        delaySetting.text = delayText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
