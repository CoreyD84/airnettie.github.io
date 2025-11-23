package com.airnettie.mobile.models

import com.airnettie.mobile.modules.EscalationMatrix

data class FlaggedMessage(
    val text: String = "",
    val severity: EscalationMatrix.Severity? = null,
    val category: EscalationMatrix.Category? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val matchedItems: List<String> = emptyList(), // phrases or emojis
    val source: String = "phrase",                // e.g. "sms", "chat", "web"
    val sourceApp: String = "",                   // optional: "Messenger", "Discord", etc.
    val messageId: String = "",                   // optional: Firebase key
    val childId: String = "",                     // optional: for filtering or export
    val householdId: String = "",                 // optional: for filtering or export
    val isEscalated: Boolean = false,             // optional: for freeze reflex or alerts
    var notes: String = "",                       // guardian notes or annotations
    val deflectionUsed: String? = null            // optional: phrase child used to deflect harm
)