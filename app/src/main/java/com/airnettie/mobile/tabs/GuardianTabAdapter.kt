@file:Suppress("unused")

package com.airnettie.mobile.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.airnettie.mobile.features.guardian.fragments.*

class GuardianTabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    val tabTitles = listOf(
        "Recent",
        "Flagged",
        "Freeze",
        "Mascot",
        "Scanner",
        "SMS",
        "Location"
    )

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RecentDetectionsFragment()
        1 -> FlaggedMessagesFragment()
        2 -> FreezeReflexFragment()
        3 -> MascotMoodFragment()
        4 -> MessageScannerFragment()
        5 -> SmsDetectionsFragment()
        6 -> LocationStatusFragment()
        else -> Fragment()
    }
}
