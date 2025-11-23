package com.airnettie.mobile.tabs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MascotMoodTab() {
    val context = LocalContext.current
    var moodHistory by remember { mutableStateOf<List<MoodEvent>>(emptyList()) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val childId = prefs.getString("child_id", null)
        val householdId = prefs.getString("household_id", null)

        if (childId.isNullOrBlank() || householdId.isNullOrBlank()) return@LaunchedEffect

        val ref = FirebaseDatabase.getInstance()
            .getReference("mascotMood/$householdId/$childId")

        ref.get().addOnSuccessListener { snapshot ->
            val moods = snapshot.children.mapNotNull { snap ->
                val mood = snap.child("mood").getValue(String::class.java) ?: return@mapNotNull null
                val timestamp = snap.child("timestamp").getValue(Long::class.java) ?: return@mapNotNull null
                val formatted = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US).format(Date(timestamp))
                MoodEvent(formatted, mood)
            }.sortedByDescending { it.timestamp }
            moodHistory = moods
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Mascot Mood History", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (moodHistory.isEmpty()) {
            Text("No mood history found.")
        } else {
            LazyColumn {
                items(moodHistory) { event ->
                    Text("🧠 ${event.timestamp}: ${event.mood}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class MoodEvent(val timestamp: String, val mood: String)