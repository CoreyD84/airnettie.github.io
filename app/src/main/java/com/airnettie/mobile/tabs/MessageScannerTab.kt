package com.airnettie.mobile.tabs

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import android.util.Log
import com.airnettie.mobile.components.ScannerEngine
import com.airnettie.mobile.features.EmotionalScanner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun MessageScannerTab() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ScannerEngine.loadPatterns(context) {
            isLoaded = true
            Log.d("MessageScannerTab", "✅ Emotional patterns loaded")
            Log.d("MessageScannerTab", "📊 Phrases: ${EmotionalScanner.loadedPhrasesByCategory}")
            Log.d("MessageScannerTab", "📊 Emojis: ${EmotionalScanner.loadedEmojisByCategory}")
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Message Scanner", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (!isLoaded) {
                    results = listOf("⚠️ Scanner not ready. Please wait for patterns to load.")
                    return@Button
                }

                if (input.isBlank()) {
                    results = listOf("⚠️ Please enter a message to scan.")
                    return@Button
                }

                Log.d("MessageScannerTab", "🔍 Scanning input: \"$input\"")

                val scanResults = ScannerEngine.scan(input)
                results = if (scanResults.isEmpty()) {
                    listOf("✅ No threats detected")
                } else {
                    scanResults.map {
                        "⚠️ Matched: ${it.matched.joinToString()}, Category: ${it.category}, Severity: ${it.severity}"
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Scan")
        }

        Spacer(modifier = Modifier.height(16.dp))

        results.forEach { result ->
            Text(result, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}