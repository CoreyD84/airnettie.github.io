package com.airnettie.mobile.features.guardian.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.airnettie.mobile.MainActivity

class ConsentOverviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to MainActivity and pass tab index for Platform tab (index 7)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("start_tab_index", 7)
        }
        startActivity(intent)
        finish()
    }
}