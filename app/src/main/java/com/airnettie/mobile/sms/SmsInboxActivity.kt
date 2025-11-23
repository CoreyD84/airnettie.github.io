package com.airnettie.mobile.sms

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airnettie.mobile.R

class SmsInboxActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_inbox)

        listView = findViewById(R.id.sms_list)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 1002)
        } else {
            loadSmsInbox()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSmsInbox()
        } else {
            Toast.makeText(this, "SMS permission denied. Cannot load inbox.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadSmsInbox() {
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)

        val messages = mutableListOf<String>()
        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex) ?: "(No content)"
                val address = it.getString(addressIndex) ?: "(Unknown sender)"
                messages.add("📨 From $address:\n$body")
            }
        }

        if (messages.isEmpty()) {
            messages.add("📭 No SMS messages found.")
        }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
    }
}