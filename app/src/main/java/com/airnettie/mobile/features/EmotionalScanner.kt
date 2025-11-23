package com.airnettie.mobile.features

import android.util.Log
import com.airnettie.mobile.modules.EscalationMatrix

object EmotionalScanner {

    private const val TAG = "EmotionalScanner"

    sealed class EscalationResult {
        abstract val isEscalated: Boolean

        data class Flagged(
            val phrase: String,
            val meta: EscalationMatrix.EscalationMeta,
            val source: String = "phrase",
            override val isEscalated: Boolean = false
        ) : EscalationResult()

        data class VeryCritical(
            val phrase: String,
            val meta: EscalationMatrix.EscalationMeta,
            val source: String = "phrase",
            override val isEscalated: Boolean = true
        ) : EscalationResult()

        data class EmojiDetected(
            val emoji: String,
            val category: String,
            val source: String = "emoji",
            override val isEscalated: Boolean = false
        ) : EscalationResult()

        object Clear : EscalationResult() {
            override val isEscalated = false
        }
    }

    val loadedPhrasesByCategory = mutableMapOf<String, List<String>>()
    val loadedEmojisByCategory = mutableMapOf<String, List<String>>()

    fun scanMessage(message: String, source: String = "phrase"): List<EscalationResult> {
        val results = mutableListOf<EscalationResult>()
        val normalized = message.lowercase().trim()

        // 🔍 Scan escalation map
        EscalationMatrix.escalationMap.forEach { (phrase, meta) ->
            if (normalized.contains(phrase.lowercase())) {
                val result = when (meta.severity) {
                    EscalationMatrix.Severity.CRITICAL,
                    EscalationMatrix.Severity.HIGH ->
                        EscalationResult.VeryCritical(phrase, meta, source)
                    else ->
                        EscalationResult.Flagged(phrase, meta, source)
                }
                results.add(result)
            }
        }

        // 🔍 Scan loaded phrases
        loadedPhrasesByCategory.forEach { (category, phrases) ->
            phrases.forEach { phrase ->
                if (normalized.contains(phrase.lowercase())) {
                    val severity = when {
                        category.contains("selfharm", true) ||
                                category.contains("grooming", true) ||
                                category.contains("sexual", true) ->
                            EscalationMatrix.Severity.CRITICAL
                        category.contains("threat", true) ->
                            EscalationMatrix.Severity.HIGH
                        else ->
                            EscalationMatrix.Severity.MEDIUM
                    }

                    val meta = EscalationMatrix.EscalationMeta(severity, EscalationMatrix.mapCategory(category))
                    val result = if (severity == EscalationMatrix.Severity.CRITICAL)
                        EscalationResult.VeryCritical(phrase, meta, source)
                    else
                        EscalationResult.Flagged(phrase, meta, source)

                    results.add(result)
                }
            }
        }

        // 🔍 Scan emojis
        loadedEmojisByCategory.forEach { (category, emojiList) ->
            emojiList.forEach { emoji ->
                if (message.contains(emoji)) {
                    results.add(EscalationResult.EmojiDetected(emoji, category))
                }
            }
        }

        // 🧠 Log outcome
        if (results.isEmpty()) {
            Log.i(TAG, "🟢 Message scanned: no threats detected.")
            return listOf(EscalationResult.Clear)
        } else {
            Log.w(TAG, "🚨 Message flagged: ${results.size} issues detected.")
            results.forEach {
                when (it) {
                    is EscalationResult.Flagged ->
                        Log.w(TAG, "⚠️ Flagged phrase: ${it.phrase} (${it.meta.severity})")
                    is EscalationResult.VeryCritical ->
                        Log.e(TAG, "🔥 Critical phrase: ${it.phrase} (${it.meta.severity})")
                    is EscalationResult.EmojiDetected ->
                        Log.d(TAG, "🔸 Emoji detected: ${it.emoji} (${it.category})")
                    else -> {}
                }
            }
        }

        return results
    }
}
