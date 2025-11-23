package com.airnettie.mobile.features.guardian.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airnettie.mobile.R
import com.google.firebase.database.*

class LocationStatusActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var statusMessage: TextView
    private lateinit var btnDirections: Button
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val defaultLat = 40.4895
    private val defaultLng = -81.9832

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_status)

        webView = findViewById(R.id.webView)
        statusMessage = findViewById(R.id.statusMessage)
        btnDirections = findViewById(R.id.btnDirections)

        webView.settings.javaScriptEnabled = true

        // ✅ Load default map first
        loadMap(defaultLat, defaultLng, "Showing default location")

        // ✅ Hook up directions button
        btnDirections.setOnClickListener {
            val uri = Uri.parse("google.navigation:q=$defaultLat,$defaultLng")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }

        checkLocationPermissions()
        listenForFirebaseLocation()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                statusMessage.text = "📍 Location permission granted. Syncing live location..."
            } else {
                statusMessage.text = "⚠️ Location permission denied. Showing default location."
            }
        }
    }

    // ✅ Listen for latest location from Firebase
    private fun listenForFirebaseLocation() {
        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val childId = prefs.getString("child_id", null)
        val householdId = prefs.getString("household_id", null)

        if (!childId.isNullOrEmpty() && !householdId.isNullOrEmpty()) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("location/$householdId/$childId")
                .limitToLast(1)

            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val lat = snapshot.child("latitude").getValue(Double::class.java) ?: defaultLat
                    val lng = snapshot.child("longitude").getValue(Double::class.java) ?: defaultLng
                    loadMap(lat, lng, "✅ Live location synced")
                }

                override fun onCancelled(error: DatabaseError) {
                    statusMessage.text = "❌ Failed to load location: ${error.message}"
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            })
        }
    }

    // ✅ Helper to inject coordinates into Leaflet HTML
    private fun loadMap(lat: Double, lng: Double, message: String) {
        statusMessage.text = message
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8" />
              <title>Child Location</title>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css" />
              <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
              <style> body { margin:0; } #map { height:90vh; } </style>
            </head>
            <body>
              <div id="map"></div>
              <script>
                const map = L.map('map').setView([$lat, $lng], 14);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                L.marker([$lat, $lng]).addTo(map).bindPopup("Child's Last Location").openPopup();
              </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}