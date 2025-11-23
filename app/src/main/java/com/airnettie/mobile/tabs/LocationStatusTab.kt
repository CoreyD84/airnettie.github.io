package com.airnettie.mobile.tabs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.airnettie.mobile.databinding.TabLocationStatusBinding

class LocationStatusTab : Fragment() {

    private var _binding: TabLocationStatusBinding? = null
    private val binding get() = _binding!!

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabLocationStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences("nettie_prefs", 0)
        val childId = prefs.getString("child_id", null)
        val householdId = prefs.getString("household_id", null)

        if (childId.isNullOrEmpty() || householdId.isNullOrEmpty()) {
            binding.locationText.text = "⚠️ Missing child or household ID"
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("location/$householdId/$childId")

        ref.limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latest = snapshot.children.firstOrNull()
                if (latest == null) {
                    binding.locationText.text = "📍 No location data available"
                    return
                }

                latitude = latest.child("latitude").getValue(Double::class.java)
                longitude = latest.child("longitude").getValue(Double::class.java)
                val timestamp = latest.child("timestamp").getValue(Long::class.java)
                val isMocked = latest.child("isMocked").getValue(Boolean::class.java) ?: false

                val timeString = timestamp?.let {
                    DateFormat.format("MMM dd, yyyy • h:mm a", it)
                } ?: "unknown time"

                val display = buildString {
                    append("📍 Last known location:\n")
                    append("Latitude: $latitude\n")
                    append("Longitude: $longitude\n")
                    append("Time: $timeString\n")
                    if (isMocked) append("⚠️ This location is mocked for demo purposes.")
                }

                binding.locationText.text = display
            }

            override fun onCancelled(error: DatabaseError) {
                binding.locationText.text = "❌ Failed to load location: ${error.message}"
            }
        })

        binding.directionsButton.setOnClickListener {
            if (latitude != null && longitude != null) {
                val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Child's Last Location)")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "No map app found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}