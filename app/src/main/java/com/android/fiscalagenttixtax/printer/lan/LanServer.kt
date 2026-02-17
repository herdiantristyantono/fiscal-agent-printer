package com.android.fiscalagenttixtax.printer.lan

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class LanServer(private val port: Int) {

    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    @Volatile
    private var running = false

    fun start(onClientConnected: (Socket) -> Unit) {

        running = true

        executor.execute {
            try {
                serverSocket = ServerSocket(port)

                while (running) {

                    val socket = serverSocket!!.accept()

                    onClientConnected(socket)
                }

            } catch (e: Exception) {
                if (running) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stop() {

        running = false

        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }

        executor.shutdownNow()
    }
}