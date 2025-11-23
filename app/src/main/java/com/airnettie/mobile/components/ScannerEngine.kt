package com.airnettie.mobile.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.airnettie.mobile.features.EmotionalScanner
import com.google.firebase.database.FirebaseDatabase
import com.airnettie.mobile.features.EmotionalPatternLoader
import com.airnettie.mobile.modules.EscalationMatrix

object ScannerEngine {

    private const val TAG = "ScannerEngine"
    var isLoaded = false

    @JvmStatic
    fun loadPatterns(context: Context, onComplete: (() -> Unit)? = null) {
        EmotionalPatternLoader.loadAllPatterns(context) {
            isLoaded = true
            Log.i(TAG, "✅ Emotional patterns loaded")
            Log.d(TAG, "🧠 isLoaded = $isLoaded after pattern load")
            Log.d(TAG, "📊 Loaded phrases: ${EmotionalScanner.loadedPhrasesByCategory}")
            Log.d(TAG, "📊 Loaded emojis: ${EmotionalScanner.loadedEmojisByCategory}")

            if (onComplete != null) {
                Handler(Looper.getMainLooper()).post {
                    onComplete()
                }
            } else {
                Log.w(TAG, "⚠️ No completion callback provided to loadPatterns")
            }
        }
    }

    data class ScanResult(
        val matched: List<String>,
        val category: EscalationMatrix.Category,
        val severity: EscalationMatrix.Severity
    )

    fun scan(phrase: String): List<ScanResult> {
        if (!isLoaded) {
            Log.w(TAG, "⚠️ Scanner not ready. Patterns not loaded.")
            return emptyList()
        }

        val normalized = phrase.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")

        Log.d(TAG, "🔍 Normalized input: \"$normalized\"")

        val results = mutableListOf<ScanResult>()

        EmotionalScanner.loadedPhrasesByCategory.forEach { (categoryKey, phrases) ->
            val matches = phrases.filter { normalized.contains(it.lowercase()) }
            if (matches.isNotEmpty()) {
                results.add(
                    ScanResult(
                        matched = matches,
                        category = EscalationMatrix.mapCategory(categoryKey),
                        severity = EscalationMatrix.mapSeverity(categoryKey)
                    )
                )
            }
        }

        EmotionalScanner.loadedEmojisByCategory.forEach { (categoryKey, emojis) ->
            val matches = emojis.filter { phrase.contains(it) }
            if (matches.isNotEmpty()) {
                results.add(
                    ScanResult(
                        matched = matches,
                        category = EscalationMatrix.mapCategory(categoryKey),
                        severity = EscalationMatrix.mapSeverity(categoryKey)
                    )
                )
            }
        }

        if (results.isEmpty()) {
            Log.d(TAG, "🟢 No matches found for: \"$phrase\"")
        } else {
            Log.w(TAG, "⚠️ Flagged: ${results.size} categories matched for \"$phrase\"")
        }

        return results
    }

    fun logDetection(context: Context, phrase: String, results: List<ScanResult>) {
        if (results.isEmpty()) return

        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val guardianId = prefs.getString("guardian_id", null)
        val householdId = prefs.getString("household_id", null)
        val childId = prefs.getString("child_id", null)

        if (guardianId.isNullOrEmpty() || householdId.isNullOrEmpty() || childId.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Missing guardian/household/child ID. Detection not logged.")
            return
        }

        Log.d(TAG, "📡 Scanning on behalf of guardian: $guardianId")

        val isEscalated = results.any {
            it.severity == EscalationMatrix.Severity.HIGH || it.severity == EscalationMatrix.Severity.CRITICAL
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("flagged_messages/$householdId/$childId")
            .push()

        val payload = mapOf(
            "original" to phrase,
            "matched" to results.flatMap { it.matched },
            "categories" to results.map { it.category.name },
            "severities" to results.map { it.severity.name },
            "timestamp" to System.currentTimeMillis(),
            "isEscalated" to isEscalated
        )

        ref.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "🚨 Detection logged to Firebase")
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Failed to log detection: ${it.localizedMessage}")
            }
    }
}