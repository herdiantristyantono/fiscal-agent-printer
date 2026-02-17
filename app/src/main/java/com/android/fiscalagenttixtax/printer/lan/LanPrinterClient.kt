package com.android.fiscalagenttixtax.printer.lan

import android.util.Log
import java.net.InetSocketAddress
import java.net.Socket

class LanPrinterClient(
    private val ip: String,
    private val port: Int
) {

    private val TAG = "LanPrinterClient"

    private var socket: Socket? = null

    fun connect(): Boolean {
        return try {

            socket = Socket()
            socket!!.connect(
                InetSocketAddress(ip, port),
                3000
            )

            Log.d(TAG, "Connected to LAN printer $ip:$port")

            true

        } catch (e: Exception) {

            Log.e(TAG, "LAN connect failed", e)
            false
        }
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (_: Exception) {}
    }
}