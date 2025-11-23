package com.airnettie.mobile.child

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import com.airnettie.mobile.modules.SafeScope

class SafeScopeVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var running = false

    override fun onCreate() {
        super.onCreate()
        Log.i("SafeScopeVpnService", "✅ SafeScope VPN service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        Log.i("SafeScopeVpnService", "🛑 SafeScope VPN service destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (running) return

        val builder = Builder()
        builder.setSession("SafeScopeVPN")
            .addAddress("10.0.0.2", 32) // virtual interface
            .addDnsServer("8.8.8.8")    // upstream DNS
            .addRoute("0.0.0.0", 0)     // route all traffic

        vpnInterface = builder.establish()
        running = true

        Thread {
            try {
                val input = FileInputStream(vpnInterface!!.fileDescriptor)
                val output = FileOutputStream(vpnInterface!!.fileDescriptor)
                val channel = DatagramChannel.open()
                channel.connect(InetSocketAddress("8.8.8.8", 53)) // DNS relay
                val buffer = ByteBuffer.allocate(32767)

                while (running) {
                    val length = input.read(buffer.array())
                    if (length > 0) {
                        val packetData = String(buffer.array(), 0, length)
                        Log.d("SafeScopeVpnService", "Packet intercepted: $packetData")

                        // 🔧 Check domain against SafeScope blocklist
                        val blocked = SafeScope.checkAndBlock(applicationContext, packetData)
                        if (blocked) {
                            Log.w("SafeScopeVpnService", "❌ Unsafe domain blocked")
                            // Drop packet (do not forward)
                            continue
                        }

                        // Forward packet if safe
                        channel.write(ByteBuffer.wrap(buffer.array(), 0, length))
                    }
                }
            } catch (e: Exception) {
                Log.e("SafeScopeVpnService", "Error in VPN loop: ${e.message}")
            }
        }.start()

        Log.i("SafeScopeVpnService", "🟢 SafeScope VPN started")
    }

    private fun stopVpn() {
        running = false
        vpnInterface?.close()
        vpnInterface = null
        Log.i("SafeScopeVpnService", "🔴 SafeScope VPN stopped")
    }
}