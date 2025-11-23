package com.airnettie.mobile.tabs

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airnettie.mobile.utils.QRUtils
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.*

data class LinkedChild(
    val name: String = "Unnamed",
    val mood: String = "unknown",
    val lastSeen: Long = 0L,
    val isEscalated: Boolean = false
)

@Composable
fun LinkedChildrenTab(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var children by remember { mutableStateOf<List<LinkedChild>>(emptyList()) }

    val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
    val guardianCode = prefs.getString("guardian_code", null)

    var token by remember { mutableStateOf(UUID.randomUUID().toString()) }
    val qrData = "nettielink://child/link?token=$token&guardianId=$guardianCode"
    val qrBitmap by remember(qrData) { mutableStateOf(QRUtils.generateQRCode(qrData, 512)) }

    // ✅ Store token in Firebase
    LaunchedEffect(token) {
        if (!guardianCode.isNullOrBlank()) {
            val pendingRef = FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianCode/pendingTokens/$token")
            pendingRef.setValue(true)
        }
    }

    // ✅ Load linked children
    LaunchedEffect(Unit) {
        if (guardianCode.isNullOrBlank()) {
            children = emptyList()
            return@LaunchedEffect
        }

        val ref = FirebaseDatabase.getInstance().getReference("guardianLinks/$guardianCode/children")
        ref.get().addOnSuccessListener { snapshot ->
            val ids = snapshot.children.mapNotNull { it.key }

            val tempList = mutableListOf<LinkedChild>()
            ids.forEach { childId ->
                val childRef = FirebaseDatabase.getInstance().getReference("childProfiles/$childId")
                childRef.get().addOnSuccessListener { childSnap ->
                    val name = childSnap.child("name").getValue(String::class.java) ?: "Unnamed"
                    val mood = childSnap.child("mood").getValue(String::class.java) ?: "unknown"
                    val lastSeen = childSnap.child("lastSeen").getValue(Long::class.java) ?: 0L
                    val isEscalated = childSnap.child("isEscalated").getValue(Boolean::class.java) ?: false
                    tempList.add(LinkedChild(name, mood, lastSeen, isEscalated))
                    children = tempList.sortedByDescending { it.lastSeen }
                }
            }
        }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text("Linked Children", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        QRCodeBox(qrBitmap = qrBitmap, onRegenerate = {
            token = UUID.randomUUID().toString()
        })

        Spacer(modifier = Modifier.height(24.dp))

        if (children.isEmpty()) {
            Text("No children linked yet.")
        } else {
            LazyColumn {
                items(children) { child ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("👤 Name: ${child.name}")
                            Text("🎭 Mood: ${child.mood}")
                            Text("🕒 Last Seen: ${DateFormat.getDateTimeInstance().format(child.lastSeen)}")

                            if (child.isEscalated) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { /* TODO: Trigger Freeze logic */ }) {
                                    Text("🚨 Freeze")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QRCodeBox(qrBitmap: android.graphics.Bitmap, onRegenerate: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📷 Scan this QR code on your child’s device to link it.", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR Code")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRegenerate) {
                Text("Generate New QR Code")
            }
        }
    }
}