package com.airnettie.mobile.modules

import android.util.Log

object EscalationMatrix {

    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }

    enum class Category {
        Grooming,
        Bullying,
        Predator,
        SelfHarm,
        SuicidalIdeation,
        Manipulation,
        Parental,
        Sexual,
        PsychologicalPressure,
        Emotional,
        Warning,
        EmotionalDistress
    }

    data class EscalationMeta(
        val severity: Severity,
        val category: Category
    )

    private val severityMap = mapOf(
        // Emotional states
        "emotion_sadness" to Severity.HIGH,
        "emotion_anger" to Severity.MEDIUM,
        "emotion_anxiety" to Severity.HIGH,
        "emotion_distress" to Severity.CRITICAL,
        "emotion_fear" to Severity.CRITICAL,
        "emotion_isolation" to Severity.CRITICAL,
        "emotion_support" to Severity.LOW,

        // Threats and triggers
        "threat_bullying" to Severity.CRITICAL,
        "threat_grooming" to Severity.CRITICAL,
        "threat_manipulation" to Severity.HIGH,
        "threat_predatory" to Severity.CRITICAL,
        "threat_self_harm" to Severity.CRITICAL,
        "threat_suicidal_ideation" to Severity.CRITICAL,
        "threat_physical" to Severity.CRITICAL,
        "threat_blackmail" to Severity.CRITICAL,
        "threat_coercion" to Severity.CRITICAL,
        "threat_emotional_blackmail" to Severity.CRITICAL,
        "threat_psychological_pressure" to Severity.CRITICAL,
        "threat_escalation_emojis" to Severity.CRITICAL,

        // Emojis and codes
        "emotion_sadness_emojis" to Severity.HIGH,
        "emotion_anger_emojis" to Severity.MEDIUM,
        "emotion_fear_emojis" to Severity.CRITICAL,
        "emotion_isolation_emojis" to Severity.CRITICAL,
        "emotion_support_emojis" to Severity.LOW,
        "threat_secrecy_emojis" to Severity.CRITICAL,
        "threat_bullying_emojis" to Severity.CRITICAL,
        "threat_grooming_emojis" to Severity.CRITICAL,
        "threat_codes" to Severity.HIGH,
        "threat_parental" to Severity.HIGH,

        // Identity and mental health
        "identity" to Severity.MEDIUM,
        "mental_health" to Severity.HIGH,
        "self_esteem" to Severity.HIGH,
        "suicidal_ideation" to Severity.CRITICAL,
        "self_harm" to Severity.CRITICAL,
        "escalation_trigger" to Severity.CRITICAL
    )

    val escalationMap: Map<String, EscalationMeta> = severityMap.mapValues { (label, severity) ->
        EscalationMeta(severity, mapLabelToCategory(label))
    }

    private fun mapLabelToCategory(label: String): Category {
        return when {
            label.contains("grooming", true) -> Category.Grooming
            label.contains("bullying", true) -> Category.Bullying
            label.contains("predator", true) -> Category.Predator
            label.contains("self_harm", true) -> Category.SelfHarm
            label.contains("suicidal", true) -> Category.SuicidalIdeation
            label.contains("manipulation", true) -> Category.Manipulation
            label.contains("parental", true) -> Category.Parental
            label.contains("sexual", true) -> Category.Sexual
            label.contains("pressure", true) -> Category.PsychologicalPressure
            label.contains("emotional", true) -> Category.Emotional
            label.contains("sadness", true) -> Category.EmotionalDistress
            label.contains("anxiety", true) -> Category.EmotionalDistress
            label.contains("fear", true) -> Category.EmotionalDistress
            label.contains("isolation", true) -> Category.EmotionalDistress
            label.contains("warning", true) -> Category.Warning
            else -> {
                Log.w("EscalationMatrix", "⚠️ Unmapped label: $label")
                Category.EmotionalDistress
            }
        }
    }

    fun mapCategory(raw: String): Category = mapLabelToCategory(raw)

    fun mapSeverity(raw: String): Severity = getSeverity(raw)

    fun getSeverity(label: String): Severity {
        return severityMap[label] ?: Severity.LOW
    }

    fun requiresFreeze(severity: Severity): Boolean {
        return severity == Severity.CRITICAL
    }

    fun requiresGuardianAlert(severity: Severity): Boolean {
        return severity == Severity.HIGH || severity == Severity.CRITICAL
    }
}