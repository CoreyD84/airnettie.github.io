package com.airnettie.mobile.features.guardian.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.airnettie.mobile.tabs.PlatformControlTab
import com.airnettie.mobile.tabs.theme.MommaMobileTheme // ✅ Theme wrapper import

class ComposePlatformControlFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MommaMobileTheme {
                    PlatformControlTab()
                }
            }
        }
    }
}