package com.airnettie.mobile.child

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.content.pm.PackageManager
import com.airnettie.mobile.modules.SafeScope

class PlatformControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        when (action) {
            // 🔧 Platform toggles
            "com.airnettie.mobile.PLATFORM_CONTROL" -> {
                val platform = intent.getStringExtra("platform")
                val enabled = intent.getBooleanExtra("enabled", true)

                Log.d("PlatformControlReceiver", "Platform=$platform enabled=$enabled")

                if (!enabled) {
                    when (platform) {
                        "messenger" -> disableApp(context, "com.facebook.orca")
                        "discord" -> disableApp(context, "com.discord")
                        "roblox" -> disableApp(context, "com.roblox.client")
                        "tiktok" -> disableApp(context, "com.zhiliaoapp.musically")
                    }
                } else {
                    // Optionally re-enable if guardian flips back on
                    when (platform) {
                        "messenger" -> enableApp(context, "com.facebook.orca")
                        "discord" -> enableApp(context, "com.discord")
                        "roblox" -> enableApp(context, "com.roblox.client")
                        "tiktok" -> enableApp(context, "com.zhiliaoapp.musically")
                    }
                }
            }

            // 🔧 SafeScope toggle
            "com.airnettie.mobile.SAFESCOPE_CONTROL" -> {
                val enabled = intent.getBooleanExtra("enabled", false)
                Log.d("PlatformControlReceiver", "SafeScope enabled=$enabled")

                if (enabled) {
                    SafeScope.activate(context)
                } else {
                    SafeScope.deactivate(context)
                }
            }
        }
    }

    private fun disableApp(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            pm.setApplicationEnabledSetting(
                packageName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Log.d("PlatformControlReceiver", "Disabled $packageName")
        } catch (e: Exception) {
            Log.e("PlatformControlReceiver", "Failed to disable $packageName: ${e.message}")
        }
    }

    private fun enableApp(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            pm.setApplicationEnabledSetting(
                packageName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Log.d("PlatformControlReceiver", "Enabled $packageName")
        } catch (e: Exception) {
            Log.e("PlatformControlReceiver", "Failed to enable $packageName: ${e.message}")
        }
    }
}