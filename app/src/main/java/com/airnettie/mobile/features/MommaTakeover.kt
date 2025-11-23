package com.airnettie.mobile.features

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

object MommaTakeover {

    private const val TAG = "MommaTakeover"

    private val genericDeflections = listOf(
        "I don’t know… I’m really tired. I think I’m going to bed.",
        "Sorry, I’m not feeling well. Maybe tomorrow.",
        "I’m kinda busy right now. Can we talk later?",
        "I need to go. My mom’s calling me.",
        "I’m not sure what to say. I’ll talk to you later."
    )

    private val categoryDeflections = mapOf(
        "Grooming" to listOf(
            "I’m not comfortable talking about that.",
            "I need to go. My mom’s calling me.",
            "I don’t think I should be sharing that."
        ),
        "Bullying" to listOf(
            "That’s not okay. I’m stepping away.",
            "I don’t deserve to be talked to like this.",
            "I’m logging off for now."
        ),
        "Manipulation" to listOf(
            "I’m not sure I trust this conversation.",
            "I need to think about this. Talk later?",
            "I’m feeling confused. I’m going to step away."
        ),
        "SelfHarm" to listOf(
            "I’m not feeling okay. I need to talk to someone.",
            "I’m going to take a break and breathe.",
            "I’m reaching out to someone I trust."
        ),
        "Sexual" to listOf(
            "I’m not okay with this. Please stop.",
            "I don’t want to talk about that.",
            "I’m leaving this chat now."
        )
    )

    fun respond(
        service: AccessibilityService,
        category: String? = null,
        overridePhrase: String? = null
    ) {
        val phrase = overridePhrase
            ?: categoryDeflections[category]?.random()
            ?: genericDeflections.random()

        val rootNode = service.rootInActiveWindow ?: run {
            Log.w(TAG, "❌ No active window found.")
            return
        }

        val inputNode = findInputField(rootNode)

        if (inputNode != null) {
            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val args = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    phrase
                )
            }
            val success = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            if (success) {
                Log.i(TAG, "✅ Inserted deflection: \"$phrase\" (category: ${category ?: "generic"})")
            } else {
                Log.w(TAG, "⚠️ Failed to insert deflection phrase.")
            }
        } else {
            Log.w(TAG, "❌ No input field found for Momma Takeover.")
        }
    }

    private fun findInputField(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if ((node.className?.contains("EditText", ignoreCase = true) == true ||
                    node.className?.contains("TextView", ignoreCase = true) == true) &&
            node.isEditable
        ) {
            return node
        }

        for (i in 0 until node.childCount) {
            findInputField(node.getChild(i))?.let { return it }
        }

        return null
    }
}
