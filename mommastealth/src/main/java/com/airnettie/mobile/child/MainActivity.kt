package com.airnettie.mobile.child

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Once permissions are granted (or denied), start sync service anyway
            startService(Intent(this, ChildSyncService::class.java))
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data
        val token = data?.getQueryParameter("token")
        val guardianId = data?.getQueryParameter("guardianId")

        if (!token.isNullOrBlank() && !guardianId.isNullOrBlank()) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/pendingTokens/$token")

            ref.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val childId = resolveChildId()
                    val linkRef = FirebaseDatabase.getInstance()
                        .getReference("guardianLinks/$guardianId/children/$childId")
                    linkRef.setValue(true)

                    ref.removeValue()
                }
                // ✅ Always request permissions before starting sync
                requestLocationPermissions()
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            // Already granted → start service directly
            startService(Intent(this, ChildSyncService::class.java))
            finish()
        } else {
            // Request permissions silently
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun resolveChildId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_child"
    }
}