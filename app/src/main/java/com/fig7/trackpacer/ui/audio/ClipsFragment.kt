package com.fig7.trackpacer.ui.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.BuildConfig
import com.fig7.trackpacer.R
import com.fig7.trackpacer.data.ClipsModel
import com.fig7.trackpacer.data.StatusModel
import com.fig7.trackpacer.databinding.FragmentClipsBinding
import com.fig7.trackpacer.manager.ClipsManager
import com.fig7.trackpacer.ui.theme.TPTheme
import com.fig7.trackpacer.util.Bool
import org.json.JSONObject
import java.io.File
import java.util.Locale

class ClipsFragment: Fragment() {
    private var binding: FragmentClipsBinding? = null
    private lateinit var clipsPreview: TextView
    private lateinit var clipsTabs: ComposeView

    private val clipsModel:  ClipsModel  by activityViewModels()
    private val statusModel: StatusModel by activityViewModels()

    private val tabList = listOf(TabItem("Waypoint Mapping", null) { WaypointScreen(it) }, TabItem("Clip Library", null) { ClipLibScreen(it) })

    // TODO: Read this from disk
    private val catList = listOf("Get ready", "Start", "Quick start", "Waypoints", "Laps", "Distance", "Profile", "Motivation", "Finish", "Status")

    private var mpClip = MediaPlayer()
    private lateinit var tts: TextToSpeech

    @Composable
    private fun WaypointScreen(clipsManager: ClipsManager) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 12.dp)) {
            Text("Distance:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
            Text("")
            Text("Profile:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
            Text("")
            Text("")
            Text("Mapping:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
        }
    }

    @Composable
    private fun CatItem(text: String, selected: Bool, clickable: () -> Unit) {
        if(selected) {
            Card(modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .clickable(onClick = { }), backgroundColor = MaterialTheme.colors.primary) {
                Text(modifier = Modifier.wrapContentSize(align = Alignment.CenterStart), text = "  $text  ", fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .clickable(onClick = clickable)) {
                Text(modifier = Modifier.wrapContentSize(align = Alignment.CenterStart), text = "  $text  ")
            }
        }
    }

    @Composable
    private fun ClipItem(clipFile: File, selected: Bool, clickable: () -> Unit) {
        val fileName = clipFile.name
        val fileBase = fileName.substringBeforeLast('.', "")
        val fileExt  = fileName.substringAfterLast('.', "")
        val iconID   = if(fileExt == "m4a") R.drawable.baseline_audiotrack_24 else R.drawable.baseline_chat_24

        /* create default audio mapping + make the service use it */
        if(selected) {
            Card(modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .clickable(onClick = { playClip(clipFile) }), backgroundColor = MaterialTheme.colors.primary) {
                Row(modifier = Modifier.wrapContentSize(align = Alignment.CenterStart)) {
                    Image(painterResource(id = iconID), contentDescription = "")
                    Text(text = "  $fileBase  ", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Surface(modifier = Modifier
                .height(36.dp)
                .fillMaxWidth()
                .clickable(onClick = { clickable(); playClip(clipFile) })) {
                Text(modifier = Modifier.wrapContentSize(align = Alignment.CenterStart), text = "  $fileBase  ")
            }
        }
    }

    @Composable
    private fun ClipLibScreen(clipsManager: ClipsManager) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(modifier = Modifier.weight(0.5f, fill = true), text = "Category", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
                Text(modifier = Modifier.weight(0.5f, fill = true), text = "Clips",    fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f, true)) {
                var selectedCat by rememberSaveable  { mutableIntStateOf(0) }
                var selectedClip by rememberSaveable { mutableIntStateOf(0) }
                LazyColumn(modifier = Modifier.weight(0.45f, fill = true)) {
                    itemsIndexed(catList) { resultIndex, resultData ->
                        CatItem(resultData, (selectedCat == resultIndex)) { selectedCat = resultIndex; selectedClip = 0 }
                    }
                }

                Spacer(modifier = Modifier.weight(0.05f, fill = true))

                LazyColumn(modifier = Modifier.weight(0.50f, fill = true)) {
                    itemsIndexed(clipsManager.clipsForCat(catList[selectedCat])) { resultIndex, resultData ->
                        ClipItem(resultData, (selectedClip == resultIndex)) { selectedClip = resultIndex }
                    }
                }
            }

            Row {
                Button(onClick = { }) {
                    Text("New")
                }

                Spacer(modifier = Modifier.width(32.dp))

                Button(onClick = { }) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(32.dp))

                Button(onClick = { }) {
                    Text("Delete")
                }
            }
        }
    }

    private fun playClip(clipFile: File) {
        mpClip.release()
        tts.stop()

        val fileExt = clipFile.name.substringAfterLast('.', "")
        if(fileExt == "m4a") playAudio(clipFile) else playTTS(clipFile)
    }

    private fun playAudio(clipFile: File) {
        mpClip = MediaPlayer.create(requireContext(), Uri.fromFile(clipFile))
        mpClip.setOnCompletionListener { mpClip.release() }
        mpClip.start()
    }

    private fun playTTS(clipFile: File) {
        mpClip.release()
        tts.stop()

        val ttsContent = clipFile.readText()
        val json       = JSONObject(ttsContent)

        var ttsLocaleStr = ""
        var ttsRate      = 1.0
        var ttsText      = ""
        var ttsVoice     = ""
        val keys: Iterator<String> = json.keys()
        for(key in keys) {
            when(key) {
                "locale" -> ttsLocaleStr = json.getString(key)
                "rate"   -> ttsRate      = json.getString(key).toDouble()
                "text"   -> ttsText      = json.getString(key)
                "voice"  -> ttsVoice     = json.getString(key)
            }
        }

        val ttsLocaleSplit = ttsLocaleStr.split("_")
        if(ttsLocaleSplit.size == 3) {
            val ttsLanguage = ttsLocaleSplit[0]
            val ttsCountry  = ttsLocaleSplit[1]
            val ttsVariant  = ttsLocaleSplit[2]
            val ttsLocale = Locale(ttsLanguage, ttsCountry, ttsVariant)

            val voices = tts.voices.filter { voice: Voice -> ((voice.locale == ttsLocale) && (voice.name == ttsVoice)) }
            if(voices.isNotEmpty()) tts.voice = voices[0]
        } else {
            tts.voice = tts.defaultVoice
        }

        tts.setSpeechRate(ttsRate.toFloat())
        val ttsWords = ttsText.split(" ")
        if(ttsWords.size == 4) {
            for(word in ttsWords) {
                // TODO: Tweak when you can listen with the countdown timer
                // Alternatively, synthesise it and adjust the rate. Or, perhaps better, allow users to adjust the delay between words.
                // Yes, the latter, I think.
                tts.speak(word, TextToSpeech.QUEUE_ADD, null, null)
                tts.playSilentUtterance(200, TextToSpeech.QUEUE_ADD, null)
            }
        } else {
            tts.speak(ttsText, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentClipsBinding.inflate(inflater, container, false)
        val clipsView = binding!!

        @Suppress("KotlinConstantConditions")
        if(BuildConfig.FLAVOR == "free") {
            val previewText =
                ("The audio page allows you to record different prompts or replace my voice with text-to-speech. " +
                 "You could also make the audio prompts tell you how far you have run, or how far you have to go, or both.\n\n" +
                 "Would you like to have a clip that tells you to sprint near the end? Would you like to hear some motivational words as you run? You can.\n\n" +
                 "Clips & Audio will only be available in the pro version of TrackPacer, which is coming soon...")

            clipsPreview = clipsView.clipsPreview
            clipsPreview.visibility = View.VISIBLE

            clipsPreview.text = previewText
        } else {
            val clipsManager = clipsModel.clipsManager

            clipsTabs = clipsView.clipsTabs
            clipsTabs.setContent {
                TPTheme {
                    Column(modifier = Modifier.fillMaxSize()) {
                        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                        TabRow(selectedTabIndex = selectedTab, backgroundColor = MaterialTheme.colors.background) {
                            tabList.forEachIndexed { index, tabItem ->
                                Tab(
                                    text = { Text(text = tabItem.title) },
                                    selected = (selectedTab == index),
                                    onClick = { selectedTab = index })
                            }
                        }

                        tabList[selectedTab].screen(clipsManager)
                    }
                }
            }

            clipsTabs.visibility = View.VISIBLE
        }

        tts = TextToSpeech(requireContext()) { }
        return clipsView.root
    }

    override fun onResume() {
        super.onResume()

        val clipsView    = binding!!
        val pacingIcon   = clipsView.clipsPacingStatus
        val phoneIcon    = clipsView.clipsPhoneStatus
        val delaySetting = clipsView.clipsDelaySetting

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