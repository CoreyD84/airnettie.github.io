package com.airnettie.mobile.features.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.airnettie.mobile.components.LocationSync

class LocationTrackerService : Service() {

    private var locationManager: LocationManager? = null

    private val locationListener = LocationListener { location ->

        try {
            LocationSync.sendLocation(this, location)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send location: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager

        if (locationManager == null) {
            Log.e(TAG, "🚫 LocationManager unavailable — using mock fallback")
            LocationSync.sendLocation(this, null)
            stopSelf()
            return START_NOT_STICKY
        }

        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (hasFine != PackageManager.PERMISSION_GRANTED && hasCoarse != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "🚫 Location permission not granted — using mock fallback")
            LocationSync.sendLocation(this, null)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, // ✅ No GPS dependency
                60000L, // every 60 seconds
                10f,    // every 10 meters
                locationListener
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to request location updates: ${e.localizedMessage}")
            LocationSync.sendLocation(this, null)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to remove location updates: ${e.localizedMessage}")
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "LocationTrackerService"
    }
}
