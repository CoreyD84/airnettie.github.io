package com.airnettie.mobile.models

import com.airnettie.mobile.modules.EscalationMatrix.Severity

data class Flag(
    val caseId: String,
    val severity: Severity,
    val message: String,
    val source: String,
    val timestamp: Long,
    val scope: String
)
