package com.android.fiscalagenttixtax.printer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.android.fiscalagenttixtax.data.fiscal.FiscalEventRepository
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEvent
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventSource
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventType
import com.android.fiscalagenttixtax.infrastructure.sequence.SequenceGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class PrinterHealthMonitor(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val forwarder: PrinterForwarder,
    private val sequenceGenerator: SequenceGenerator
) {
    private val HEARTBEAT = byteArrayOf(0x1B, 0x40)

    fun isPrinterBound(allowedMacs: Set<String>): Boolean {
        return try {
            if (!hasBluetoothPermission()) return false

            adapter.bondedDevices.any { device ->
                allowedMacs.contains(device.address)
            }
        } catch (e: SecurityException) {
            false
        }
    }

    fun startHeartbeat(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(30_000)
                forwarder.forward(HEARTBEAT)
            }
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun reportBypass(
        repository: FiscalEventRepository
    ) = CoroutineScope(Dispatchers.IO).launch {

        val lastHash = repository.getLastHash()

        repository.save(
            event = FiscalEvent(
                id = UUID.randomUUID().toString(),
                type = FiscalEventType.BYPASS,
                source = FiscalEventSource.SYSTEM,
                amount = 0,
                receiptHash = "",
                previousHash = lastHash,
                chainHash = "",
                receiptRef = "HEALTH_CHECK",
                referenceEventId = null,
                createdAt = System.currentTimeMillis(),
                monotonicTime = SystemClock.elapsedRealtime(),
                sequence = sequenceGenerator.next()
            ),
            receiptRef = null
        )
    }
}