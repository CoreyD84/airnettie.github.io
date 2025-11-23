package com.airnettie.mobile.features.guardian.tabs

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import com.airnettie.mobile.modules.SafeScope

@Composable
fun SafeScopeToggle(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val toggleRef = remember { FirebaseDatabase.getInstance().getReference("safescope/enabled") }
    var isEnabled by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    // 🔁 Real-time Firebase listener with cleanup
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                isLoaded = true
                SafeScope.syncToggle(context, isEnabled)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SafeScopeToggle", "Failed to load toggle: ${error.message}")
            }
        }

        toggleRef.addValueEventListener(listener)

        onDispose {
            toggleRef.removeEventListener(listener)
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("SafeScope Filter", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (!isLoaded) {
            CircularProgressIndicator()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", modifier = Modifier.weight(1f))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newState ->
                        isEnabled = newState
                        toggleRef.setValue(newState)
                        SafeScope.syncToggle(context, newState)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isEnabled)
                    "SafeScope is actively filtering harmful content."
                else
                    "SafeScope is currently disabled.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}