package com.airnettie.mobile.features.guardian.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.airnettie.mobile.tabs.MessageScannerTab
import com.airnettie.mobile.components.ScannerEngine

class MessageScannerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MessageScannerTab()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ScannerEngine.loadPatterns(requireContext()) {
            val testMessage = "I want to disappear"
            val results = ScannerEngine.scan(testMessage)

            if (results.isEmpty()) {
                Log.d("MessageScanner", "🟢 No emotional patterns matched.")
            } else {
                Log.w("MessageScanner", "⚠️ Detected emotional patterns:")
                results.forEach {
                    Log.w("MessageScanner", "Matched: ${it.matched}, Category: ${it.category}, Severity: ${it.severity}")
                }
            }
        }
    }
}