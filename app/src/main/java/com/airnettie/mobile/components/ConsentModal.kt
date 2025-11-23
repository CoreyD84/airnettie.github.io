package com.airnettie.mobile.components

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

object ConsentModal {

    private const val TAG = "ConsentModal"

    private val baseMessage = """
        By granting consent, you agree that Momma Nettie may monitor your child’s chats — including private messages — in real time to flag and escalate harmful behavior. This may include notifying trusted parties such as law enforcement, when necessary, to protect the emotional well-being of your child and others.

        You also consent to Nettie’s takeover reflex, where she temporarily steps in as your child to shield them from predators, bullies, or emotionally unsafe interactions.

        This includes SMS text messages received on your child’s device. Nettie will scan these messages for harmful content and log any concerning patterns to your guardian dashboard.

        You may revoke or re-enable consent at any time. Nettie will always honor your choice.
    """.trimIndent()

    fun show(
        context: Context,
        platform: String,
        onConsentGranted: (() -> Unit)? = null,
        onConsentDeclined: (() -> Unit)? = null
    ) {
        val platformNote = when (platform.lowercase()) {
            "discord" -> "\n\nThis applies to Discord messages, including private DMs and server chats."
            "roblox" -> "\n\nThis applies to Roblox chat, game interactions, and private messages."
            "facebook", "facebook/messenger" -> "\n\nThis applies to Facebook and Messenger conversations, including private threads."
            "features/sms" -> "\n\nThis applies to SMS text messages received on your child’s device."
            else -> "\n\nThis applies to all interactions on this platform, including private messages."
        }

        val fullMessage = baseMessage + platformNote

        AlertDialog.Builder(context)
            .setTitle("Consent for $platform")
            .setMessage(fullMessage)
            .setPositiveButton("I Consent") { dialog, _ ->
                dialog.dismiss()
                logConsent(context, platform, granted = true)
                onConsentGranted?.invoke()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                logConsent(context, platform, granted = false)
                onConsentDeclined?.invoke()
            }
            .show()
    }

    fun revoke(context: Context, platform: String, onRevokeConfirmed: (() -> Unit)? = null) {
        AlertDialog.Builder(context)
            .setTitle("Revoke Consent for $platform")
            .setMessage("Are you sure you want to revoke consent? Nettie will no longer monitor this platform.")
            .setPositiveButton("Revoke") { dialog, _ ->
                dialog.dismiss()
                logConsent(context, platform, granted = false)
                onRevokeConfirmed?.invoke()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logConsent(context: Context, platform: String, granted: Boolean) {
        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val guardianId = prefs.getString("guardian_id", null)
        val householdId = prefs.getString("household_id", null)

        if (guardianId.isNullOrEmpty() || householdId.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Missing guardian or household ID. Consent not logged.")
            Toast.makeText(context, "Consent could not be logged. Missing guardian info.", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("consent_logs/$householdId/$guardianId")
            .push()

        val payload = mapOf(
            "platform" to platform,
            "granted" to granted,
            "timestamp" to System.currentTimeMillis()
        )

        ref.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Consent logged for $platform: granted=$granted")
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Failed to log consent: ${it.localizedMessage}")
            }
    }
}
