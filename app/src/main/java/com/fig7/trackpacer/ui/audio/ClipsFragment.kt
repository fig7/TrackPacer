package com.fig7.trackpacer.ui.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fig7.trackpacer.R
import com.fig7.trackpacer.databinding.FragmentClipsBinding

class ClipsFragment: Fragment() {
    private var binding: FragmentClipsBinding? = null

    private lateinit var clipsPreview: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentClipsBinding.inflate(inflater, container, false)
        val clipsView = binding!!

        val previewText = ("The audio page allows you to record different prompts or replace my voice with text-to-speech. "
                + "You could also make the audio prompts tell you how far you have run, or how far you have to go, or both.\n\n"
                + "Would you like to have a clip that tells you to sprint near the end? Would you like to hear some motivational words as you run? You can.\n\n"
                + "Clips and Audio will only be available in the pro version of TrackPacer, which is coming soon...")

        clipsPreview = clipsView.clipsPreview
        clipsPreview.text = previewText

        return clipsView.root
    }

    override fun onResume() {
        super.onResume()

        val clipsView = binding!!
        val context = requireContext()

        val phoneIcon = clipsView.clipsPhoneStatus
        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = clipsView.clipsDelaySetting
        delaySetting.setText(R.string.start_delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}