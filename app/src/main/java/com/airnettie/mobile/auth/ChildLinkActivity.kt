package com.airnettie.mobile.auth

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.airnettie.mobile.MainActivity
import com.airnettie.mobile.databinding.ActivityChildLinkBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChildLinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildLinkBinding
    private lateinit var cameraExecutor: ExecutorService
    private val scanner = BarcodeScanning.getClient()
    private var hasLinked = false

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        } else {
            startCamera()
        }
    }

    @ExperimentalGetImage
    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    val rotation = imageProxy.imageInfo.rotationDegrees

                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, rotation)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue ?: continue
                                    if (!hasLinked && rawValue.startsWith("nettielink://")) {
                                        hasLinked = true
                                        val householdId = rawValue.removePrefix("nettielink://")
                                        linkToHousehold(householdId)
                                        imageProxy.close()
                                        return@addOnSuccessListener
                                    }
                                }
                                imageProxy.close()
                            }
                            .addOnFailureListener {
                                imageProxy.close()
                                Toast.makeText(this, "Scan failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun linkToHousehold(householdId: String) {
        val childId = FirebaseAuth.getInstance().currentUser?.uid
        if (childId == null) {
            Toast.makeText(this, "No child ID found. Please log in.", Toast.LENGTH_LONG).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("linked_children/$householdId/$childId")

        val nickname = Build.MODEL ?: "Child Device"
        val lastSeen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.time.LocalDateTime.now().toString()
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        }
        val mood = "calm"

        val update = mapOf(
            "nickname" to nickname,
            "last_seen" to lastSeen,
            "mood" to mood
        )

        ref.updateChildren(update).addOnSuccessListener {
            getSharedPreferences("nettie_prefs", MODE_PRIVATE).edit {
                putString("household_id", householdId)
            }

            Toast.makeText(this, "Linked to household: $householdId", Toast.LENGTH_LONG).show()
            promptStealthMode()
        }.addOnFailureListener {
            Toast.makeText(this, "Link failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun promptStealthMode() {
        AlertDialog.Builder(this)
            .setTitle("Enable Stealth Mode?")
            .setMessage("Momma Mobile can quietly protect you in the background. Would you like to hide the app icon from your home screen?")
            .setPositiveButton("Yes, hide it") { _, _ ->
                try {
                    val pm = packageManager
                    val componentName = ComponentName(this, MainActivity::class.java)
                    pm.setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    Toast.makeText(this, "Stealth Mode enabled. App icon hidden.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to enable Stealth Mode: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    promptSmsConsent()
                }
            }
            .setNegativeButton("No, keep visible") { _, _ ->
                promptSmsConsent()
            }
            .setCancelable(false)
            .show()
    }

    private fun promptSmsConsent() {
        AlertDialog.Builder(this)
            .setTitle("Enable Emotional Radar?")
            .setMessage("To help protect you from harmful messages, Nettie needs permission to become your default SMS app. This allows her to scan incoming texts for emotional safety risks. No messages are stored or shared — only flagged patterns are reported to your guardian.")
            .setPositiveButton("Enable Emotional Radar") { _, _ ->
                requestSmsRoleIfAvailable()
            }
            .setNegativeButton("Not now") { _, _ ->
                Toast.makeText(this, "Emotional radar not enabled. You can turn it on later in settings.", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestSmsRoleIfAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivity(intent)
            } else {
                Toast.makeText(this, "SMS role not available on this device.", Toast.LENGTH_LONG).show()
            }
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}