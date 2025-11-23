package com.airnettie.mobile.features

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class FeelScopeService : AccessibilityService() {

    private var takeoverTriggered = false

    override fun onServiceConnected() {
        EmotionalPatternLoader.loadAllPatterns(this) {
            Log.i(TAG, "✅ Emotional patterns loaded. Scanner is ready.")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val rawText = event.text?.joinToString(" ")?.lowercase()?.trim().orEmpty()
        if (rawText.isBlank()) return

        val results = EmotionalScanner.scanMessage(rawText)
        val sourceApp = event.packageName?.toString().orEmpty()

        results.forEach { result ->
            when (result) {
                is EmotionalScanner.EscalationResult.Flagged -> {
                    Log.i(TAG, "⚠️ Flagged: ${result.phrase} [${result.meta.category}]")
                    FirebaseLogger.logDetection(
                        context = this,
                        severity = "Flagged",
                        message = rawText,
                        matchedPhrases = listOf(result.phrase),
                        category = result.meta.category.name,
                        source = result.source,
                        sourceApp = sourceApp,
                        isEscalated = result.isEscalated
                    )
                }

                is EmotionalScanner.EscalationResult.VeryCritical -> {
                    Log.w(TAG, "🔥 Very Critical: ${result.phrase} [${result.meta.category}]")
                    FirebaseLogger.logDetection(
                        context = this,
                        severity = "VeryCritical",
                        message = rawText,
                        matchedPhrases = listOf(result.phrase),
                        category = result.meta.category.name,
                        source = result.source,
                        sourceApp = sourceApp,
                        isEscalated = result.isEscalated
                    )

                    if (!takeoverTriggered) {
                        performGlobalAction(GLOBAL_ACTION_HOME)
                        // // MommaTakeover.respond(this, category = result.meta.category.name)
                        takeoverTriggered = true
                    }
                }

                is EmotionalScanner.EscalationResult.EmojiDetected -> {
                    Log.i(TAG, "🔸 Emoji detected: ${result.emoji} [${result.category}]")
                    FirebaseLogger.logDetection(
                        context = this,
                        severity = "EmojiDetected",
                        message = rawText,
                        matchedPhrases = listOf(result.emoji),
                        category = result.category,
                        source = result.source,
                        sourceApp = sourceApp,
                        isEscalated = result.isEscalated
                    )
                }

                EmotionalScanner.EscalationResult.Clear -> {
                    Log.d(TAG, "🟢 No threats detected in this window.")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "⚠️ Accessibility service interrupted.")
    }

    companion object {
        private const val TAG = "FeelScope"
    }
}
