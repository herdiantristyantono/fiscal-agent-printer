package com.android.fiscalagentixtax.printer.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.SystemClock
import android.util.Log
import com.android.fiscalagentixtax.receipt.escpos.EscPosBuffer
import com.android.fiscalagenttixtax.data.fiscal.FiscalEventRepository
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEvent
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventSource
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventType
import com.android.fiscalagenttixtax.domain.fiscal.FiscalHashChain
import com.android.fiscalagenttixtax.infrastructure.sequence.SequenceGenerator
import com.android.fiscalagenttixtax.printer.PrinterForwarder
import com.android.fiscalagenttixtax.queue.PrintQueue
import com.android.fiscalagenttixtax.receipt.escpos.EscPosParser
import com.android.fiscalagenttixtax.security.ReceiptHasher
import java.io.InputStream
import java.util.UUID

class VirtualBluetoothPrinter(
    private val forwarder: PrinterForwarder,
    private val queue: PrintQueue,
    private val fiscalRepository: FiscalEventRepository,
    private val sequenceGenerator: SequenceGenerator
) {

    val TAG = "VirtualBluetoothPrinter"
    private val buffer = EscPosBuffer()
    private val parser = EscPosParser()

    suspend fun handleClient(socket: BluetoothSocket) {
        val input: InputStream = socket.inputStream
        val readBuffer = ByteArray(4096)

        Log.d(TAG, "VirtualBluetoothPrinter App is running")

        while (true) {
            val bytesRead = input.read(readBuffer)
            if (bytesRead <= 0) break

            val rawData = readBuffer.copyOf(bytesRead)

            // 1️⃣ Forward ke printer asli
            forwarder.forward(rawData)

            // 2️⃣ Queue (retry / offline)
            queue.enqueue(rawData)

            // 3️⃣ Accumulate ESC/POS
            buffer.append(rawData)

            // 4️⃣ Receipt boundary
            if (!buffer.isReceiptEnd(rawData)) continue

            val fullReceipt = buffer.flush()

            val (receipt, type) = parser.parse(fullReceipt)

            val receiptHash = ReceiptHasher.sha256(fullReceipt)

            val lastHash = fiscalRepository.getLastHash()

            val monotonic = SystemClock.elapsedRealtime()
            val sequence = sequenceGenerator.next()

            // 🔗 Cari SALE kalau REFUND / VOID
            val referencedSale =
                if (type != FiscalEventType.SALE && receipt.reference != null) {
                    fiscalRepository.findSaleByReference(receipt.reference)
                } else null

            val amount =
                if (type == FiscalEventType.SALE)
                    receipt.totalAmount
                else
                    -receipt.totalAmount
            val existingEvent =
                fiscalRepository.findByReceiptHash(receiptHash)

            if (existingEvent != null) {
                // 🚨 DUPLICATE / REPRINT DETECTED
                Log.w("FiscalAgent", "Duplicate print detected: ${existingEvent.id}")

                val reprintEvent = FiscalEvent(
                    id = UUID.randomUUID().toString(),
                    type = FiscalEventType.REPRINT,
                    source = FiscalEventSource.BLUETOOTH_PRINTER,
                    amount = 0,
                    receiptHash = receiptHash,
                    previousHash = lastHash,
                    chainHash = "",
                    monotonicTime = monotonic,
                    sequence = sequence,
                    referenceEventId = existingEvent.id,
                    createdAt = System.currentTimeMillis(),
                    refEventId = referencedSale?.id
                )

                val chainHash = FiscalHashChain.compute(
                    previousHash = lastHash,
                    receiptRaw = fullReceipt,
                    event = reprintEvent
                )

                fiscalRepository.save(
                    event = reprintEvent.copy(chainHash = chainHash),
                    receiptRef = receipt.reference
                )

                continue // ⛔ JANGAN buat SALE baru
            }

            val lastEventTime = fiscalRepository.getLastEventTime()

            if (lastEventTime != null) {
                val gap = System.currentTimeMillis() - lastEventTime
                if (gap > 30 * 60 * 1000) {
                    fiscalRepository.save(
                        event = FiscalEvent(
                            id = UUID.randomUUID().toString(),
                            type = type,
                            source = FiscalEventSource.PRINTER,
                            amount = if (type == FiscalEventType.SALE) receipt.totalAmount else -receipt.totalAmount,
                            receiptHash = receiptHash,
                            previousHash = lastHash,
                            chainHash = "",
                            monotonicTime = monotonic,   // ✅ FIX ERROR #6
                            sequence = sequence,         // ✅ FIX ERROR #6
                            createdAt = System.currentTimeMillis(),
                            refEventId = referencedSale?.id
                        ),
                        receiptRef = null
                    )
                }
            }

            val event = FiscalEvent(
                id = UUID.randomUUID().toString(),
                type = type,
                source = FiscalEventSource.PRINTER,
                amount = if (type == FiscalEventType.SALE) receipt.totalAmount else -receipt.totalAmount,
                receiptHash = receiptHash,
                previousHash = lastHash,
                chainHash = "",
                monotonicTime = monotonic,   // ✅ FIX ERROR #6
                sequence = sequence,         // ✅ FIX ERROR #6
                createdAt = System.currentTimeMillis(),
                refEventId = referencedSale?.id
            )

            val chainHash = FiscalHashChain.compute(
                previousHash = lastHash,
                receiptRaw = fullReceipt,
                event = event
            )

            val finalEvent = event.copy(chainHash = chainHash)

            fiscalRepository.save(
                event = finalEvent,
                receiptRef = receipt.reference
            )
        }
    }
}