package com.airnettie.mobile.features

import android.content.Context
import android.util.Log
import com.google.firebase.database.*

object EmotionalPatternLoader {

    private const val TAG = "EmotionalPatternLoader"

    fun loadAllPatterns(context: Context, onComplete: () -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("/") // ✅ Load from root

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                EmotionalScanner.loadedPhrasesByCategory.clear()
                EmotionalScanner.loadedEmojisByCategory.clear()

                snapshot.children.forEach { categorySnapshot ->
                    val category = categorySnapshot.key ?: return@forEach

                    // ✅ Only load emotion_* and threat_* categories
                    if (!category.startsWith("emotion_") && !category.startsWith("threat_")) return@forEach

                    val values = categorySnapshot.children.mapNotNull {
                        it.getValue(String::class.java)
                    }.filter { it.isNotBlank() }

                    if (values.isEmpty()) {
                        Log.w(TAG, "⚠️ Skipped empty category: $category")
                        return@forEach
                    }

                    // ✅ Route based on content type
                    if (values.any { it.contains(Regex("[a-zA-Z]")) }) {
                        EmotionalScanner.loadedPhrasesByCategory[category] = values
                        Log.d(TAG, "📥 Loaded phrases for [$category]: ${values.joinToString()}")
                    } else {
                        EmotionalScanner.loadedEmojisByCategory[category] = values
                        Log.d(TAG, "📥 Loaded emojis for [$category]: ${values.joinToString()}")
                    }
                }

                Log.i(
                    TAG,
                    "✅ Patterns loaded: ${EmotionalScanner.loadedPhrasesByCategory.size} phrase categories, " +
                            "${EmotionalScanner.loadedEmojisByCategory.size} emoji categories"
                )
                onComplete()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Failed to load emotional patterns: ${error.message}")
                HarmfulPatterns.loadFallbackEmojis() // ✅ Safe fallback trigger
                onComplete() // ✅ Ensure completion callback is always called
            }
        })
    }
}