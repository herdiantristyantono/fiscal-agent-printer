package com.android.fiscalagenttixtax.printer.lan

import android.util.Log
import com.android.fiscalagenttixtax.infrastructure.PrintLogger
import com.android.fiscalagenttixtax.infrastructure.status.StatusController
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket

class LanPrintServer(
    private val port: Int,
    private val onData: (ByteArray) -> Unit
) {

    private val TAG = "LanPrintServer"

    private var server: ServerSocket? = null

    fun start() {

        CoroutineScope(Dispatchers.IO).launch {

            server = ServerSocket(port)

            Log.d(TAG, "LAN server started on port $port")

            while (true) {

                val client: Socket = server!!.accept()

                Log.d(TAG, "POS connected: ${client.inetAddress}")
                StatusController.setPosConnected(true)

                handleClient(client)
            }
        }
    }

    private fun handleClient(socket: Socket) {

        CoroutineScope(Dispatchers.IO).launch {

            val input = socket.getInputStream()
            val buffer = ByteArray(4096)

            while (true) {

                val len = input.read(buffer)

                if (len <= 0) break

                val data = buffer.copyOf(len)

                // 🔍 LOG DATA DARI POS
                PrintLogger.logRaw(data)

                // ➡️ Forward ke bluetooth printer
                onData(data)
            }

            socket.close()

            Log.d(TAG, "POS disconnected")
            StatusController.setPosConnected(false)
        }
    }

    fun stop() {
        server?.close()
    }
}