package com.airnettie.mobile.features.guardian.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airnettie.mobile.R
import com.airnettie.mobile.utils.QRUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class GenerateLinkQrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_link_qr)

        val guardianId = FirebaseAuth.getInstance().currentUser?.uid
        if (guardianId == null) {
            Toast.makeText(this, "Guardian not signed in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val token = UUID.randomUUID().toString()

        // ✅ Correct GitHub Pages domain for redirect
        val redirectUrl = "https://airnettie.github.io/link?token=$token&guardianId=$guardianId"

        // ✅ Store token in Firebase
        val ref = FirebaseDatabase.getInstance()
            .getReference("guardianLinks/$guardianId/pendingTokens/$token")
        ref.setValue(true)

        // ✅ Generate QR code using QRUtils
        val qrBitmap = QRUtils.generateQRCode(redirectUrl)

        // ✅ Display QR code
        findViewById<ImageView>(R.id.qrImageView).setImageBitmap(qrBitmap)
    }
}