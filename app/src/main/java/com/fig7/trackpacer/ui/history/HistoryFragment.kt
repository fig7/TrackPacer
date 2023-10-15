package com.fig7.trackpacer.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material.Text
import androidx.fragment.app.Fragment

class HistoryFragment: Fragment () {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LazyColumn {
                    itemsIndexed(listOf("Hello", "everyone", "today")) { _, string ->
                        Text(string)
                    }
                }
            }
        }
    }
}
