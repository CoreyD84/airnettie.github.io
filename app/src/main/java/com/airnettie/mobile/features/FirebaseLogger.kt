package com.airnettie.mobile.features

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseLogger {

    private const val TAG = "FirebaseLogger"

    // 🔍 Logs emotional detections (phrases, emojis, SMS, etc.)
    fun logDetection(
        context: Context,
        severity: String,
        message: String,
        matchedPhrases: List<String>,
        category: String? = null,
        source: String = "phrase",       // "phrase", "emoji", "sms", "chat"
        sourceApp: String? = null,       // e.g. "Messenger", "Discord"
        isEscalated: Boolean = false     // freeze reflex or alert triggered
    ) {
        val householdId = getHouseholdId(context)
        val childId = getChildId(context)
        val timestamp = getTimestamp()

        if (householdId.isNullOrEmpty() || childId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Missing householdId or childId — cannot log detection.")
            Log.e(TAG, "Message: \"$message\" | Severity: $severity | Source: $source")
            return
        }

        val basePath = if (source == "emoji") "emojis" else "detections"
        val detectionRef = FirebaseDatabase.getInstance()
            .getReference("feelscope/$basePath/$childId")
            .push()

        val payload = mutableMapOf(
            "message" to message,
            "matchedPhrases" to matchedPhrases,
            "severity" to severity,
            "timestamp" to timestamp,
            "source" to source,
            "isEscalated" to isEscalated,
            "package" to context.packageName
        ).apply {
            category?.let { this["category"] = it }
            sourceApp?.let { this["sourceApp"] = it }
        }

        Log.d(TAG, "🧠 Logging $source detection payload: $payload")

        detectionRef.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Detection logged to $basePath: $severity | ${matchedPhrases.size} matches")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "❌ Failed to log detection: ${error.localizedMessage}")
            }
    }

    // 🧠 Logs general events under /logs/{uid}
    fun logEvent(uid: String, type: String, message: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("logs/$uid")
            .push()

        val payload = mapOf(
            "type" to type,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        ref.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Logged event: [$type] $message")
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Failed to log event: ${it.localizedMessage}")
            }
    }

    // 🔐 Logs system-level events using current Firebase user
    fun logSystem(type: String, message: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        logEvent(uid, type, message)
    }

    private fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }

    private fun getHouseholdId(context: Context): String? =
        context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            .getString("household_id", null)

    private fun getChildId(context: Context): String? =
        context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            .getString("child_id", null)
}
