package com.airnettie.mobile.features.guardian.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.airnettie.mobile.R

class ConsentOverviewTab : Fragment() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var database: FirebaseDatabase
    private var householdId: String? = null
    private var guardianId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_consent_overview, container, false)
        containerLayout = view.findViewById(R.id.consentContainer)
        database = FirebaseDatabase.getInstance()

        val prefs = requireContext().getSharedPreferences("nettie_prefs", android.content.Context.MODE_PRIVATE)
        householdId = prefs.getString("household_id", null)
        guardianId = prefs.getString("guardian_id", null)

        if (!householdId.isNullOrEmpty() && !guardianId.isNullOrEmpty()) {
            loadConsentStatus()
        } else {
            showMessage("Guardian identity missing. Please log in again.")
        }

        return view
    }

    private fun loadConsentStatus() {
        val ref = database.getReference("consent_status/$householdId/$guardianId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerLayout.removeAllViews()
                if (!snapshot.exists()) {
                    showMessage("No consent records found.")
                    return
                }

                snapshot.children.forEach { platformSnapshot ->
                    val platform = platformSnapshot.key ?: return@forEach
                    val granted = platformSnapshot.getValue(Boolean::class.java) ?: false

                    val statusText = TextView(requireContext()).apply {
                        text = "$platform: ${if (granted) "✅ Granted" else "❌ Revoked"}"
                        setPadding(16, 8, 16, 8)
                    }
                    containerLayout.addView(statusText)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("Failed to load consent status: ${error.message}")
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

    override fun onDestroyView() {
        super.onDestroyView()
        containerLayout.removeAllViews()
    }
}
