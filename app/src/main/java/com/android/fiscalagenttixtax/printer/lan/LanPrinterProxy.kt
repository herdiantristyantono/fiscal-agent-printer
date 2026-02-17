package com.android.fiscalagenttixtax.printer.lan

import com.android.fiscalagenttixtax.printer.PrinterForwarder
import com.android.fiscalagenttixtax.queue.PrintQueue
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.Socket

class LanPrinterProxy(
    private val forwarder: PrinterForwarder,
    private val queue: PrintQueue
) {

    private val server = LanServer(9100)

    fun start() {

        server.start { socket: Socket ->

            CoroutineScope(Dispatchers.IO).launch {

                val input: InputStream = socket.getInputStream()
                val buffer = ByteArray(4096)

                while (true) {

                    val len = input.read(buffer)
                    if (len <= 0) break

                    val raw = buffer.copyOf(len)

                    // 👉 Forward to printer
                    forwarder.forward(raw)

                    // 👉 Queue for fiscal
                    queue.enqueue(raw)
                }

                socket.close()
            }
        }
    }

    fun stop() {
        server.stop()
    }
}