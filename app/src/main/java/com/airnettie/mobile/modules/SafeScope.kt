@file:Suppress("SpellCheckingInspection")

package com.airnettie.mobile.modules

import android.content.Context
import android.util.Log
import com.google.firebase.database.*
import com.airnettie.mobile.modules.EscalationMatrix.Severity
import com.airnettie.mobile.modules.EscalationMatrix.Category

object SafeScope {

    private const val TAG = "SafeScope"

    private val blockedDomains = listOf(
        // Suicide/self-harm forums
        "suicideforum.com", "sanctionedsuicide.com", "selfharmhub.net", "darkthoughts.org", "lostallhope.com",
        // Explicit adult content
        "pornhub.com", "xvideos.com", "xnxx.com", "redtube.com", "youjizz.com", "brazzers.com", "onlyfans.com",
        "fapello.com", "rule34.xxx", "xhamster.com", "spankbang.com", "tnaflix.com", "camwhores.tv",
        "leakgirls.com", "nudostar.com",
        // Unsafe chatrooms
        "omegle.com", "chatroulette.com", "chathub.cam", "dirtyroulette.com"
    )

    private var toggleRef: DatabaseReference? = null
    private var valueEventListener: ValueEventListener? = null

    fun activate(context: Context) {
        Log.i(TAG, "✅ SafeScope activated — listening for Firebase toggle")

        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val householdId = prefs.getString("household_id", null)
        val childId = prefs.getString("child_id", null)

        if (householdId.isNullOrBlank() || childId.isNullOrBlank()) {
            Log.w(TAG, "Missing householdId or childId — skipping toggle listener.")
            return
        }

        toggleRef = FirebaseDatabase.getInstance()
            .getReference("safescope/toggles/$householdId/$childId")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                if (isEnabled) {
                    Log.i(TAG, "🟢 SafeScope toggle ON — scanning enabled")
                    // TODO: Start scanning logic here
                } else {
                    Log.i(TAG, "🔴 SafeScope toggle OFF — scanning disabled")
                    // TODO: Stop scanning logic here
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Firebase listener cancelled: ${error.message}")
            }
        }

        toggleRef?.addValueEventListener(valueEventListener!!)
    }

    fun deactivate(context: Context) {
        Log.i(TAG, "🛑 SafeScope deactivated — listener removed")
        toggleRef?.removeEventListener(valueEventListener!!)
        toggleRef = null
        valueEventListener = null
    }

    fun syncToggle(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val householdId = prefs.getString("household_id", null)
        val childId = prefs.getString("child_id", null)

        if (householdId.isNullOrBlank() || childId.isNullOrBlank()) {
            Log.w(TAG, "Missing householdId or childId — skipping toggle sync.")
            return
        }

        Log.i(TAG, "🔁 Syncing SafeScope toggle to Firebase: $enabled")
        FirebaseDatabase.getInstance()
            .getReference("safescope/toggles/$householdId/$childId")
            .setValue(enabled)
    }

    fun checkAndBlock(context: Context, url: String): Boolean {
        val matched = blockedDomains.find { domain ->
            url.contains(domain, ignoreCase = true)
        }

        if (matched != null) {
            val caseId = System.currentTimeMillis().toString()
            val severity = "critical"
            val message = "Blocked access to $matched"

            Log.d("DomainBlocker", "Blocked domain detected: $matched")

            FirebaseSync.syncFlag(caseId, severity, message)
            MascotMood.setMoodFromSeverity(severity)

            return true  // ✅ Blocked
        }

        return false  // ✅ Safe
    }
}