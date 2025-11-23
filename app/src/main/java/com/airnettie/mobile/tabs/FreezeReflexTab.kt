@file:Suppress("unused")

package com.airnettie.mobile.tabs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.airnettie.mobile.R
import java.text.SimpleDateFormat
import java.util.*

class FreezeReflexTab : Fragment() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var database: FirebaseDatabase
    private var householdId: String? = null
    private var childId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_freeze_reflex, container, false)
        containerLayout = view.findViewById(R.id.freezeReflexContainer)
        database = FirebaseDatabase.getInstance()

        if (!isAdded) return view

        val prefs = requireContext().getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        householdId = prefs.getString("household_id", null)
        childId = prefs.getString("child_id", null)

        if (householdId != null && childId != null) {
            loadFreezeEvents()
        } else {
            val errorText = TextView(requireContext()).apply {
                text = "Missing guardian or child identity. Please log in again."
                setPadding(16, 16, 16, 16)
            }
            containerLayout.addView(errorText)
        }

        return view
    }

    private fun loadFreezeEvents() {
        val ref = database.getReference("feelscope/households/$householdId/detections/$childId")
        ref.orderByChild("isEscalated").equalTo(true.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    containerLayout.removeAllViews()

                    if (!snapshot.exists()) {
                        val emptyText = TextView(requireContext()).apply {
                            text = "No freeze reflex events recorded."
                            setPadding(16, 16, 16, 16)
                        }
                        containerLayout.addView(emptyText)
                        return
                    }

                    val sorted = snapshot.children.sortedByDescending { it.key }
                    for (event in sorted) {
                        val category = event.child("category").getValue(String::class.java) ?: "Unknown"
                        val matched = event.child("matchedPhrases").children.mapNotNull { it.getValue(String::class.java) }
                        val sourceApp = event.child("sourceApp").getValue(String::class.java) ?: "Unknown"
                        val timestampRaw = event.child("timestamp").getValue(Long::class.java)
                        val deflection = event.child("deflectionUsed").getValue(String::class.java)

                        val formattedTime = timestampRaw?.let {
                            val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US)
                            sdf.format(Date(it))
                        } ?: event.key ?: "Unknown"

                        val eventText = TextView(requireContext()).apply {
                            text = buildString {
                                append("🚨 Freeze Reflex Triggered\n")
                                append("🧭 Category: $category\n")
                                append("🔍 Matched: ${matched.joinToString(", ")}\n")
                                append("📱 App: $sourceApp\n")
                                append("🕒 Time: $formattedTime\n")
                                if (!deflection.isNullOrBlank()) {
                                    append("🛡️ Deflection: \"$deflection\"\n")
                                }
                            }
                            setPadding(16, 12, 16, 12)
                        }
                        containerLayout.addView(eventText)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded) return
                    val errorText = TextView(requireContext()).apply {
                        text = "Failed to load freeze reflex history: ${error.message}"
                        setPadding(16, 16, 16, 16)
                    }
                    containerLayout.addView(errorText)
                }
            })
    }
}