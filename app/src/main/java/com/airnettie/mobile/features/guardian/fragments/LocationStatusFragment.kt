package com.airnettie.mobile.features.guardian.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airnettie.mobile.features.guardian.activities.LocationStatusActivity

class LocationStatusFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        startActivity(Intent(requireContext(), LocationStatusActivity::class.java))
        return View(requireContext()) // return an empty view to satisfy the fragment
    }
}