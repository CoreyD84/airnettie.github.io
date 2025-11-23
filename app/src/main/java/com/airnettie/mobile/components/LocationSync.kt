package com.airnettie.mobile.components

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object LocationSync {

    private const val TAG = "LocationSync"

    fun sendLocation(context: Context, location: Location?) {
        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val childId = prefs.getString("child_id", null)
        val householdId = prefs.getString("household_id", null)

        if (childId.isNullOrEmpty() || householdId.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Missing child or household ID. Location not sent.")
            return
        }

        // 🧪 Use mock location if null
        val latitude = location?.latitude ?: 40.4958  // Killbuck, OH
        val longitude = location?.longitude ?: -81.9832

        val ref = FirebaseDatabase.getInstance()
            .getReference("location/$householdId/$childId")
            .push()

        val data = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis(),
            "isMocked" to (location == null)
        )

        ref.setValue(data)
            .addOnSuccessListener {
                Log.i(TAG, "📍 Location sent: $data")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to send location: ${e.localizedMessage}")
            }
    }
}
