package com.airnettie.mobile.child

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase

class ChildSyncService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()

        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val childId = prefs.getString("child_id", null) ?: return START_NOT_STICKY

        updateHeartbeat(childId)
        syncLocation(childId)

        return START_STICKY
    }

    private fun updateHeartbeat(childId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("childProfiles/$childId")
        ref.child("lastSeen").setValue(System.currentTimeMillis())
        ref.child("mood").setValue("calm")
    }

    private fun syncLocation(childId: String) {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = LocationManager.GPS_PROVIDER

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(provider)
            location?.let {
                val payload = mapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                val locRef = FirebaseDatabase.getInstance().getReference("location/$childId")
                locRef.setValue(payload)
            }
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "nettielocation"
        val channelName = "Nettie Location Sync"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nettie Child Sync")
            .setContentText("Location sync active")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}