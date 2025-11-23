package com.airnettie.mobile.features

import android.content.Context
import android.util.Log

object FreezeReflex {

    private const val TAG = "FreezeReflex"

    fun activate(context: Context, source: String, message: String) {
        Log.d(TAG, "FreezeReflex activated for message from $source: $message")
    }
}
