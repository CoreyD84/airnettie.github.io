package com.airnettie.mobile.modules

import android.content.Context
import android.util.Log
import com.airnettie.mobile.features.FirebaseLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object MascotMood {

    private const val TAG = "MascotMood"

    private var currentMood: String = "calm"

    fun getMood(): String = currentMood

    fun setMoodFromSeverity(severity: String) {
        currentMood = when (severity.lowercase()) {
            "critical" -> "alert"
            "high" -> "concerned"
            "medium" -> "watchful"
            "low" -> "calm"
            else -> "neutral"
        }

        Log.i(TAG, "🧠 Mascot mood set to: $currentMood (from severity: $severity)")
        FirebaseLogger.logSystem("mood_set", "Mascot mood set from severity: $severity → $currentMood")
    }

    fun startMoodSync(context: Context) {
        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val householdId = prefs.getString("household_id", null)
        val childId = prefs.getString("child_id", null)

        if (householdId.isNullOrBlank() || childId.isNullOrBlank()) {
            Log.w(TAG, "Missing householdId or childId — skipping mood sync.")
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("feelscope/households/$householdId/detections/$childId")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recentFlags = snapshot.children.mapNotNull {
                    it.child("timestamp").getValue(Long::class.java)
                }

                val previousMood = currentMood
                currentMood = when {
                    recentFlags.isEmpty() -> "calm"
                    recentFlags.any { isWithinLastHour(it) } -> "concerned"
                    recentFlags.size >= 5 -> "alert"
                    else -> "watchful"
                }

                syncMoodToFirebase(householdId, childId, currentMood)

                if (currentMood != previousMood) {
                    FirebaseLogger.logSystem("mood_change", "Mood shifted from $previousMood to $currentMood")
                }

                if (recentFlags.isNotEmpty()) {
                    FirebaseLogger.logSystem("flag_detected", "Detected ${recentFlags.size} emotional flags")
                }

                if (currentMood == "alert") {
                    FirebaseLogger.logSystem("reflex_trigger", "FreezeReflex activated due to high-severity phrase count")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Mood sync failed: ${error.message}")
            }
        })
    }

    private fun isWithinLastHour(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        return now - timestamp < 60 * 60 * 1000
    }

    private fun syncMoodToFirebase(householdId: String, childId: String, mood: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("mascotMood/$householdId/$childId")
        ref.push().setValue(
            mapOf(
                "mood" to mood,
                "timestamp" to System.currentTimeMillis()
            )
        )
        Log.d(TAG, "Mascot mood synced: $mood")
    }
}