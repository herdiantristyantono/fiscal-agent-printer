package com.android.fiscalagenttixtax.printer.usb

import android.hardware.usb.*
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.android.fiscalagentixtax.receipt.escpos.EscPosBuffer
import com.android.fiscalagenttixtax.printer.PrinterForwarder
import com.android.fiscalagenttixtax.queue.PrintQueue
import com.android.fiscalagenttixtax.data.fiscal.FiscalEventRepository
import com.android.fiscalagenttixtax.infrastructure.sequence.SequenceGenerator
import com.android.fiscalagenttixtax.domain.fiscal.*
import com.android.fiscalagenttixtax.infrastructure.status.StatusController
import com.android.fiscalagenttixtax.receipt.escpos.EscPosParser
import com.android.fiscalagenttixtax.security.ReceiptHasher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class UsbPrinterProxy(
    private val context: Context,
    private val forwarder: PrinterForwarder,
    private val queue: PrintQueue,
    private val fiscalRepository: FiscalEventRepository,
    private val sequenceGenerator: SequenceGenerator
) {

    private val usbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val buffer = EscPosBuffer()
    private val parser = EscPosParser()

    private val _status = MutableStateFlow(UsbStatus.WAITING)
    val status: StateFlow<UsbStatus> = _status

    private var running = false
    private var job: Job? = null

    fun start() {
        if (running) return
        running = true

        job = CoroutineScope(Dispatchers.IO).launch {
            listenUsb()
        }
    }

    fun stop() {
        running = false
        job?.cancel()
        job = null

        Log.d("UsbPrinterProxy", "USB proxy stopped")
    }

    private suspend fun listenUsb() {

        while (true) {

            val device = usbManager.deviceList.values.firstOrNull()
            if (device == null) {
                StatusController.setUsbPlugged(false)
                delay(1000)
                continue
            }

            if (!usbManager.hasPermission(device)) {
                StatusController.setUsbPlugged(false)
                delay(1000)
                continue
            }

            try {
                val connection = usbManager.openDevice(device)
                StatusController.setUsbPlugged(true)
                val intf = device.getInterface(0)
                val endpoint = intf.getEndpoint(0)

                connection.claimInterface(intf, true)

                val readBuffer = ByteArray(4096)

                while (true) {
                    val len = connection.bulkTransfer(
                        endpoint,
                        readBuffer,
                        readBuffer.size,
                        3000
                    )

                    if (len <= 0) continue

                    val raw = readBuffer.copyOf(len)

                    // 🔁 forward to real printer
                    forwarder.forward(raw)

                    // 📦 queue
                    queue.enqueue(raw)

                    // 📑 fiscal capture
                    buffer.append(raw)

                    if (buffer.isReceiptEnd(raw)) {

                        val fullReceipt = buffer.flush()
                        handleReceipt(fullReceipt)
                    }
                }
            } catch (e: Exception) {
                _status.value = UsbStatus.ERROR
                StatusController.setUsbPlugged(false)
                delay(1000)
            }
        }
    }

    private suspend fun handleReceipt(full: ByteArray) {

        val (receipt, type) = parser.parse(full)

        val receiptHash = ReceiptHasher.sha256(full)

        val lastHash = fiscalRepository.getLastHash()

        val monotonic = SystemClock.elapsedRealtime()
        val sequence = sequenceGenerator.next()

        val receiptRef = receipt.reference ?: UUID.randomUUID().toString()

        val event = FiscalEvent(
            id = UUID.randomUUID().toString(),
            type = type,
            source = FiscalEventSource.USB,
            amount = if (type == FiscalEventType.SALE)
                receipt.totalAmount else -receipt.totalAmount,
            receiptHash = receiptHash,
            previousHash = lastHash,
            chainHash = "",
            receiptRef = receiptRef,     // ✅ ADD THIS
            referenceEventId = null,     // sementara null
            createdAt = System.currentTimeMillis(),
            monotonicTime = monotonic,
            sequence = sequence
        )

        val chainHash = FiscalHashChain.compute(
            previousHash = lastHash,
            receiptRaw = full,
            event = event
        )

        fiscalRepository.save(
            event.copy(chainHash = chainHash),
            receiptRef
        )
    }
}