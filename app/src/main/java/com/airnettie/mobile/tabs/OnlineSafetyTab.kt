package com.airnettie.mobile.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnlineSafetyTab(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val safetyTips = listOf(
        "💬 Talk regularly with your child about what they see and feel online.",
        "🔒 Use platform privacy settings to limit who can message or friend your child.",
        "🧠 Teach your child to recognize emotional manipulation and pressure tactics.",
        "📵 Encourage breaks from screens to reset emotional balance.",
        "👥 Join your child on platforms like Roblox or Discord to understand their experience.",
        "🚫 Block or report users who make your child feel unsafe — and let them know it’s okay.",
        "🛡️ Use Nettie’s emotional radar to detect distress early and respond with care.",
        "📍 Keep location sharing off unless absolutely necessary.",
        "🎮 Remind your child that online games are not private — others can see and hear them.",
        "📖 Share stories of online safety wins to build confidence and emotional resilience."
    )

    val currentTimeMillis = System.currentTimeMillis()
    val hoursSinceEpoch = currentTimeMillis / (1000 * 60 * 60)
    val tipIndex = ((hoursSinceEpoch / 4) % safetyTips.size).toInt()
    val currentTip = safetyTips[tipIndex]

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text("Online Safety Tip", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(currentTip, modifier = Modifier.padding(12.dp))
        }
    }
}