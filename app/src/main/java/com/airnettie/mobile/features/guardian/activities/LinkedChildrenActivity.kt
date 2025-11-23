package com.airnettie.mobile.features.guardian.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airnettie.mobile.R
import com.airnettie.mobile.features.guardian.fragments.LinkedChildrenFragment

class LinkedChildrenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linked_children)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LinkedChildrenFragment())
            .commit()
    }
}