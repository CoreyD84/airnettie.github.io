package com.airnettie.mobile.features

import android.util.Log

object HarmfulPatterns {

    // ✅ Dynamic emoji map — populated from Firebase
    val emojiMap = mutableMapOf<String, MutableList<String>>()

    // ✅ Add emoji to a category
    fun addEmoji(category: String, emoji: String) {
        val list = emojiMap.getOrPut(category) { mutableListOf() }
        if (emoji !in list) list.add(emoji)
    }

    // ✅ Optional fallback static lists — now using snake_case to match Firebase
    private val fallbackEmojis = mapOf(
        "emotion_sadness_emojis" to listOf("😢", "😭", "💔", "😞", "🫥", "😔", "😿", "🥀"),
        "emotion_anger_emojis" to listOf("😡", "🤬", "👊", "💣", "🖕", "💀", "🧨", "🗯️", "🔪", "🧌"),
        "emotion_fear_emojis" to listOf("😨", "😰", "😱", "🫣", "🧠", "😧", "😟", "😬"),
        "emotion_isolation_emojis" to listOf("📵", "🚫", "🙅‍♂️", "🙅‍♀️", "🧍‍♂️", "🧍‍♀️", "🫥", "🕳️"),
        "emotion_support_emojis" to listOf("❤️", "🤗", "🫶", "🧸", "🙏", "🌈", "☀️", "💬", "🫂", "⭐"),
        "threat_bullying_emojis" to listOf("😡", "🤬", "👊", "💣", "🖕", "💀", "🧨", "🗯️", "🔪", "🧌", "😾", "😤"),
        "threat_grooming_emojis" to listOf("🍑", "🍆", "💦", "👅", "😈", "🫦", "🛏️", "📩", "🔒", "🧴", "🩲", "🫳", "🕳️", "🫣", "🧍‍♂️", "🧍‍♀️", "🌽", "🍜", "👀", "🤤", "🔨", "🌶️"),
        "threat_manipulation_emojis" to listOf("🕵️‍♂️", "🫥", "📩", "🔒", "🕳️", "🫳", "🙃", "🧠", "🫣", "🧍‍♂️", "🧍‍♀️"),
        "threat_secrecy_emojis" to listOf("🔒", "📩", "🕳️", "🫥", "🕵️‍♂️", "🙈", "🙉", "🙊"),
        "threat_escalation_emojis" to listOf("🔥", "💣", "🔪", "🧨", "😈", "👿", "🗯️", "💀")
    )

    private var fallbackLoaded = false

    // ✅ Load fallback emojis only when explicitly called
    fun loadFallbackEmojis() {
        if (fallbackLoaded) return
        fallbackLoaded = true

        fallbackEmojis.forEach { (category, emojis) ->
            emojiMap[category] = emojis.toMutableList()
        }

        Log.w("HarmfulPatterns", "⚠️ Firebase emoji load failed — fallback emojis loaded")
    }

    // ✅ Expose all emojis for scanner integration
    fun getAllEmojis(): Map<String, List<String>> {
        return emojiMap.mapValues { it.value.toList() }
    }
}