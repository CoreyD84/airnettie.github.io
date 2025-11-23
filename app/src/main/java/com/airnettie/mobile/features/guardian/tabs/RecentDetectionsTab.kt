package com.airnettie.mobile.features.guardian.tabs

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.airnettie.mobile.R

@Suppress("SpellCheckingInspection")
class RecentDetectionsTab : Fragment() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var database: FirebaseDatabase
    private var householdId: String? = null
    private var childId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_recent_detections, container, false)
        containerLayout = view.findViewById(R.id.detectionsContainer)
        database = FirebaseDatabase.getInstance()

        val prefs = requireContext().getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        householdId = prefs.getString("household_id", null)
        childId = prefs.getString("child_id", null)

        if (!householdId.isNullOrEmpty() && !childId.isNullOrEmpty()) {
            Log.d("RecentDetectionsTab", "Loading detections for $householdId/$childId")
            loadDetections()
        } else {
            showMessage("⚠️ Missing guardian or child identity. Please log in again.")
            Log.w("RecentDetectionsTab", "Missing householdId or childId")
        }

        return view
    }

    private fun loadDetections() {
        val ref = database.getReference("feelscope/households/$householdId/detections/$childId")
        ref.orderByKey().limitToLast(25).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerLayout.removeAllViews()
                if (!snapshot.exists()) {
                    showMessage("No recent detections found.")
                    Log.d("RecentDetectionsTab", "No detections found in Firebase")
                    return
                }

                val sorted = snapshot.children.sortedByDescending { it.key }
                Log.d("RecentDetectionsTab", "Found ${sorted.size} detections")

                sorted.forEach { detection ->
                    val severity = detection.child("severity").getValue(String::class.java) ?: "Unknown"
                    val matched = detection.child("matchedPhrases").children.mapNotNull { it.getValue(String::class.java) }
                    val category = detection.child("category").getValue(String::class.java) ?: "Uncategorized"
                    val sourceApp = detection.child("sourceApp").getValue(String::class.java) ?: "Unknown"
                    val timestampRaw = detection.child("timestamp").getValue(Long::class.java)
                    val timestamp = timestampRaw?.let {
                        DateFormat.format("MMM dd, yyyy • h:mm a", it).toString()
                    } ?: detection.key ?: "Unknown"
                    val isEscalated = detection.child("isEscalated").getValue(Boolean::class.java) ?: false

                    val detectionText = TextView(requireContext()).apply {
                        text = buildString {
                            append("[$severity] $category")
                            if (isEscalated) append(" 🚨")
                            append("\nMatched: ${matched.joinToString(", ")}")
                            append("\nApp: $sourceApp")
                            append("\nTime: $timestamp")
                        }
                        setPadding(16, 12, 16, 12)
                        val colorRes = when (severity.lowercase()) {
                            "critical" -> R.color.severity_critical
                            "medium" -> R.color.severity_medium
                            "low" -> R.color.severity_low
                            else -> R.color.black
                        }
                        setTextColor(ContextCompat.getColor(requireContext(), colorRes))
                    }

                    containerLayout.addView(detectionText)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("❌ Failed to load detections: ${error.message}")
                Log.e("RecentDetectionsTab", "Firebase error: ${error.message}")
            }
        })
    }

    private fun showMessage(message: String) {
        val errorText = TextView(requireContext()).apply {
            text = message
            setPadding(16, 16, 16, 16)
        }
        containerLayout.addView(errorText)
    }
}