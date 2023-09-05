package com.fig7.trackpacer.ui.completion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fig7.trackpacer.CompletionActivity
import com.fig7.trackpacer.databinding.FragmentCompletionBinding

class CompletionFragment: Fragment() {
    private var binding: FragmentCompletionBinding? = null
    private lateinit var afm: FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompletionBinding.inflate(inflater, container, false)
        val completionView = binding!!

        val mainActivity = activity as CompletionActivity
        afm = mainActivity.supportFragmentManager

        val closeButton = completionView.buttonClose
        closeButton.setOnClickListener { afm.setFragmentResult("CLOSE_ME", Bundle()) }

        return completionView.root
    }
}