package com.airnettie.mobile.tabs

import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airnettie.mobile.components.ConsentModal
import com.airnettie.mobile.tabs.theme.MommaMobileTheme
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.graphics.Color

@Composable
fun PlatformControlTab(modifier: Modifier = Modifier) {
    MommaMobileTheme {
        val context = LocalContext.current
        val platforms = listOf("Discord", "Roblox", "TikTok", "Messenger")
        val toggles = remember { mutableStateMapOf<String, Boolean>() }
        val currentTip = remember { mutableStateOf("📄 Share stories of online safety wins...") }

        val childId = remember {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown_device"
        }

        LaunchedEffect(Unit) {
            val ref = FirebaseDatabase.getInstance().getReference("platformControls/$childId")
            ref.get().addOnSuccessListener { snapshot ->
                platforms.forEach { platform ->
                    val enabled = snapshot.child(platform).getValue(Boolean::class.java) ?: true
                    toggles[platform] = enabled
                }
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium)
                        Text("• Killbuck Police Dept: (330) 555-1212")
                        Text("• Killbuck Elementary: (330) 555-3434")
                        Text("• Holmes County Sheriff: (330) 555-9876")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Online Safety Tip", style = MaterialTheme.typography.titleMedium)
                        Text(currentTip.value)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val tips = listOf(
                            "📄 Share stories of online safety wins...",
                            "🛡️ Review flagged messages together.",
                            "💬 Ask your child how Nettie feels to them.",
                            "📱 Keep platform access aligned with emotional state.",
                            "👂 Listen before reacting to emotional spikes."
                        )
                        currentTip.value = tips.random()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Tip")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Safety Tip")
                }
            }

            item {
                Text("Platform Controls", style = MaterialTheme.typography.headlineSmall)
            }

            items(platforms) { platform ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(platform)
                    Switch(
                        checked = toggles[platform] ?: true,
                        onCheckedChange = { isEnabled ->
                            toggles[platform] = isEnabled
                            val ref = FirebaseDatabase.getInstance()
                                .getReference("platformControls/$childId/$platform")
                            ref.setValue(isEnabled)

                            if (isEnabled) {
                                ConsentModal.show(context, platform)
                            }
                        }
                    )
                }
            }

            item {
                Text(
                    "Toggles above control both access and Nettie’s emotional radar per platform.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Divider()
                SafeScopeToggle()
                Divider()
                ConsentSection(childId = childId)
            }
        }
    }
}

@Composable
fun SafeScopeToggle(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val toggleRef = remember { FirebaseDatabase.getInstance().getReference("safescope/enabled") }
    var isEnabled by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        toggleRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                isLoaded = true
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("SafeScopeToggle", "Failed to load toggle: ${error.message}")
            }
        })
    }

    Column(modifier = modifier.fillMaxWidth()) {
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

@Composable
fun ConsentSection(childId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showToast by remember { mutableStateOf<String?>(null) }

    showToast?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            showToast = null
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Consent & Emotional Monitoring", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("By granting consent, you allow Nettie to:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text("• Monitor emotional signals across supported platforms (SMS, Discord, Roblox, etc.)")
                Text("• Detect harmful patterns and escalate when needed")
                Text("• Share emotional insights with you through the guardian dashboard")
                Text("• Respect boundaries and only intervene when safety is at risk")
                Text("• Log emotional spikes and matched phrases for review")
                Text("• Use mascot mood overlays to gently reflect your child’s emotional state")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "This consent can be revoked at any time. Nettie will never override your authority — she’s here to support, not replace.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                val ref = FirebaseDatabase.getInstance().getReference("consent/$childId")
                ref.setValue(true)
                showToast = "Consent granted for all platforms"
            }) {
                Text("Grant Consent")
            }
            Button(onClick = {
                val ref = FirebaseDatabase.getInstance().getReference("consent/$childId")
                ref.setValue(false)
                showToast = "Consent revoked"
            }) {
                Text("Revoke Consent")
            }
        }
    }
}