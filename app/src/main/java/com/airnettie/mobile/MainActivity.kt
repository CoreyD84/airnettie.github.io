package com.airnettie.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.airnettie.mobile.auth.LoginActivity
import com.airnettie.mobile.modules.SafeScope
import com.airnettie.mobile.mobilenettie.GuardianDashboard
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private lateinit var toggleRef: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Listen for SafeScope toggle changes from Firebase
        toggleRef = FirebaseDatabase.getInstance().getReference("safescope/enabled")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                if (isEnabled) {
                    SafeScope.activate(applicationContext)
                } else {
                    SafeScope.deactivate(applicationContext)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: log or escalate
            }
        }
        toggleRef.addValueEventListener(valueEventListener!!)

        // ✅ Launch GuardianDashboard with no tab index — mascot only
        val intent = Intent(this, GuardianDashboard::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let {
            toggleRef.removeEventListener(it)
        }
    }
}