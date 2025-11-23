package com.airnettie.mobile.mobilenettie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.airnettie.mobile.R
import com.airnettie.mobile.features.guardian.activities.*

class GuardianDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_dashboard)

        // ✅ Tab buttons now launch full-screen activities
        findViewById<Button>(R.id.btnRecent).setOnClickListener {
            startActivity(Intent(this, RecentDetectionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnFlagged).setOnClickListener {
            startActivity(Intent(this, FlaggedMessagesActivity::class.java))
        }

        findViewById<Button>(R.id.btnFreeze).setOnClickListener {
            startActivity(Intent(this, FreezeReflexActivity::class.java))
        }

        findViewById<Button>(R.id.btnLinked).setOnClickListener {
            startActivity(Intent(this, LinkedChildrenActivity::class.java))
        }

        findViewById<Button>(R.id.btnMascot).setOnClickListener {
            startActivity(Intent(this, MascotMoodActivity::class.java))
        }

        findViewById<Button>(R.id.btnScanner).setOnClickListener {
            startActivity(Intent(this, MessageScannerActivity::class.java))
        }

        findViewById<Button>(R.id.btnPlatforms).setOnClickListener {
            startActivity(Intent(this, PlatformControlActivity::class.java))
        }

        findViewById<Button>(R.id.btnSms).setOnClickListener {
            startActivity(Intent(this, SmsDetectionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLocation).setOnClickListener {
            startActivity(Intent(this, LocationStatusActivity::class.java))
        }

        // ✅ New: Launch QR generator for linking child device
        findViewById<Button>(R.id.btnLinkChild).setOnClickListener {
            startActivity(Intent(this, GenerateLinkQrActivity::class.java))
        }
    }
}