package com.airnettie.mobile.tabs

import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SmsDetectionsTab(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var flaggedMessages by remember { mutableStateOf<List<String>>(emptyList()) }

    val childId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"
    }

    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("flaggedSMS/$childId")
        ref.get().addOnSuccessListener { snapshot ->
            val messages = snapshot.children.mapNotNull {
                it.child("body").getValue(String::class.java)
            }
            flaggedMessages = messages
        }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text("Flagged SMS Messages", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (flaggedMessages.isEmpty()) {
            Text("No flagged messages found.")
        } else {
            LazyColumn {
                items(flaggedMessages) { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(msg, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}