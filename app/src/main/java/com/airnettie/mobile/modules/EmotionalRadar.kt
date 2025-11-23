package com.airnettie.mobile.modules

import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.airnettie.mobile.components.ScannerEngine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.airnettie.mobile.features.FreezeReflex

object EmotionalRadar {

    private const val TAG = "EmotionalRadar"

    fun scanSMS(context: Context) {
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
            null, null, Telephony.Sms.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            while (it.moveToNext()) {
                val body = it.getString(bodyIndex) ?: continue
                val results = ScannerEngine.scan(body)
                if (results.any { EscalationMatrix.requiresGuardianAlert(it.severity) }) {
                    ScannerEngine.logDetection(context, body, results)
                }
            }
        }
    }

    fun scanThirdParty(context: Context, message: String, source: String) {
        val results = ScannerEngine.scan(message)
        if (results.any { EscalationMatrix.requiresGuardianAlert(it.severity) }) {
            ScannerEngine.logDetection(context, message, results)
        }
    }

    // Optional fallback if Firebase fails to load patterns
    private val distressKeywords = listOf(
        "I want to disappear", "nobody cares", "I hate myself", "why am I alive",
        "you're worthless", "go kill yourself", "I wish I was dead", "stop talking to me",
        "I'm scared", "he touched me", "she won't stop", "I can't breathe", "I feel trapped"
    )

    private fun containsDistress(text: String): Boolean {
        return distressKeywords.any { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
    }

    // Optional legacy fallback
    private fun flagMessage(context: Context, source: String, message: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.w(TAG, "No authenticated user — skipping flag.")
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("flags/$uid")
        val flag = mapOf(
            "source" to source,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "severity" to EscalationMatrix.Severity.CRITICAL.name
        )
        ref.push().setValue(flag)
        Log.d(TAG, "⚠️ Legacy flag triggered from $source: $message")

        FreezeReflex.activate(context, source, message)
    }
}