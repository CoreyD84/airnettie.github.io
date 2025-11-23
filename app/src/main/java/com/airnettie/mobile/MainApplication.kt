package com.airnettie.mobile

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MainApplication", "✅ App started")
        try {
            FirebaseApp.initializeApp(this) // ✅ Global Firebase init
            Log.d("MainApplication", "✅ Firebase initialized")
        } catch (e: Exception) {
            Log.e("MainApplication", "❌ Firebase init failed", e)
        }
    }
}