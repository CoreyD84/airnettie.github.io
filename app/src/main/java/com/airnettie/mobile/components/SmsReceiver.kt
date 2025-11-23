package com.airnettie.mobile.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.airnettie.mobile.features.EmotionalScanner
import com.google.firebase.database.FirebaseDatabase

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 🛡️ Spoof protection: validate intent
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val messageBody = sms.messageBody
            val sender = sms.originatingAddress ?: "Unknown"
            Log.d(TAG, "📩 SMS from $sender: $messageBody")

            val scanResults = EmotionalScanner.scanMessage(messageBody, source = "sms")
            val flagged = scanResults.filterNot { it is EmotionalScanner.EscalationResult.Clear }

            if (flagged.isEmpty()) {
                Log.d(TAG, "🟢 SMS is emotionally safe")
                continue
            }

            val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            val guardianId = prefs.getString("guardian_id", null)
            val householdId = prefs.getString("household_id", null)
            val childId = prefs.getString("child_id", null)

            if (guardianId.isNullOrEmpty() || householdId.isNullOrEmpty() || childId.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ Missing guardian/household/child ID. SMS not logged.")
                continue
            }

            val ref = FirebaseDatabase.getInstance()
                .getReference("sms_logs/$householdId/$childId/messages")
                .push()

            val matched = flagged.mapNotNull {
                when (it) {
                    is EmotionalScanner.EscalationResult.Flagged -> it.phrase
                    is EmotionalScanner.EscalationResult.VeryCritical -> it.phrase
                    is EmotionalScanner.EscalationResult.EmojiDetected -> it.emoji
                    else -> null
                }
            }

            val categories = flagged.mapNotNull {
                when (it) {
                    is EmotionalScanner.EscalationResult.Flagged -> it.meta.category.name
                    is EmotionalScanner.EscalationResult.VeryCritical -> it.meta.category.name
                    is EmotionalScanner.EscalationResult.EmojiDetected -> it.category
                    else -> null
                }
            }

            val severities = flagged.mapNotNull {
                when (it) {
                    is EmotionalScanner.EscalationResult.Flagged -> {
                        val meta = it.meta
                        meta.severity.name
                    }
                    is EmotionalScanner.EscalationResult.VeryCritical -> {
                        val meta = it.meta
                        meta.severity.name
                    }
                    else -> null
                }
            }

            val payload = mapOf(
                "sender" to sender,
                "body" to messageBody,
                "matched" to matched,
                "categories" to categories,
                "severities" to severities,
                "timestamp" to System.currentTimeMillis()
            )

            ref.setValue(payload)
                .addOnSuccessListener {
                    Log.i(TAG, "✅ SMS detection logged to Firebase")
                }
                .addOnFailureListener {
                    Log.e(TAG, "❌ Failed to log SMS detection: ${it.localizedMessage}")
                }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
