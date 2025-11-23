package com.airnettie.mobile.features.guardian.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.airnettie.mobile.R
import com.airnettie.mobile.features.guardian.fragments.RecentDetectionsFragment

class RecentDetectionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Force full-screen rendering and solid background to prevent dashboard bleed
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setBackgroundDrawableResource(android.R.color.white)

        setContentView(R.layout.activity_recent_detections)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RecentDetectionsFragment())
            .commit()
    }
}