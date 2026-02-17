package com.android.fiscalagenttixtax.app

import android.app.Application
import android.util.Log
import com.android.fiscalagenttixtax.data.fiscal.FiscalEventRepository
import com.android.fiscalagenttixtax.data.fiscal.db.FiscalDatabase
import com.android.fiscalagenttixtax.infrastructure.sequence.SequenceGenerator
import com.android.fiscalagenttixtax.infrastructure.status.PrinterMode
import com.android.fiscalagenttixtax.infrastructure.status.StatusController
import com.android.fiscalagenttixtax.printer.LanToBluetoothForwarder
import com.android.fiscalagenttixtax.printer.PrinterForwarder
import com.android.fiscalagenttixtax.printer.bluetooth.BluetoothPrinterClient
import com.android.fiscalagenttixtax.printer.lan.LanPrintServer
import com.android.fiscalagenttixtax.printer.lan.LanPrinterClient
import com.android.fiscalagenttixtax.printer.usb.EscPosUsbWriter
import com.android.fiscalagenttixtax.printer.usb.UsbPrinterManager
import com.android.fiscalagenttixtax.printer.usb.UsbPrinterProxy
import com.android.fiscalagenttixtax.queue.PrintQueue
import com.android.fiscalagenttixtax.storage.PrinterConfig
import com.android.fiscalagenttixtax.storage.PrinterType
import com.android.fiscalagenttixtax.util.PrinterConfigStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class App : Application() {

    private var TAG = "FiscalAgentApp"

    private lateinit var usbProxy: UsbPrinterProxy
    private lateinit var bluetoothClient: BluetoothPrinterClient
    private var reconnectJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Fiscal Agent Application started")
    }

    // ====================================================
    // COMMON INIT (shared infra)
    // ====================================================

    private fun buildRepository(): FiscalEventRepository {
        val db = FiscalDatabase.getInstance(this)
        return FiscalEventRepository(db.fiscalEventDao())
    }

    private fun buildQueue(): PrintQueue {
        return PrintQueue.getInstance(this)
    }

    private fun buildSequence(): SequenceGenerator {
        return SequenceGenerator()
    }

    // ====================================================
    // START BLUETOOTH VIRTUAL PRINTER
    // ====================================================

    fun startFiscalAgentLanToBluetooth() {

        val mac = PrinterConfigStore.loadMac(this)

        if (mac.isNullOrEmpty()) {
            Log.e(TAG, "Printer MAC not set")
            return
        }

        bluetoothClient = BluetoothPrinterClient(mac)

        startAutoReconnect()

        val forwarder = LanToBluetoothForwarder(bluetoothClient)

        val lanServer = LanPrintServer(9100) { data ->
            saveRawPrint(data)
            forwarder.forward(data)
        }

        lanServer.start()

        Log.d(TAG, "LAN ➜ Bluetooth Fiscal Agent started")
    }


    // ====================================================
    // START USB PRINTER AGENT
    // ====================================================

    fun startFiscalAgentUsb() {

        val printerConfig = PrinterConfig(
            type = PrinterType.USB
        )

        val repository = buildRepository()
        val queue = buildQueue()
        val sequence = buildSequence()

        val usbManager = UsbPrinterManager(this)
        val usbWriter = EscPosUsbWriter()

        val forwarder = PrinterForwarder(
            context = applicationContext,
            config = printerConfig,
            printQueue = queue,
            usbManager = usbManager,
            usbWriter = usbWriter
        )

        usbProxy = UsbPrinterProxy(
            context = this,
            forwarder = forwarder,
            queue = queue,
            fiscalRepository = repository,
            sequenceGenerator = sequence
        )

        usbProxy.start()

        Log.d(TAG, "USB Fiscal Agent started")
    }

    // ====================================================
    // ACCESSORS
    // ====================================================

    private fun startAutoReconnect() {

        if (reconnectJob != null) return

        reconnectJob = CoroutineScope(Dispatchers.IO).launch {

            while (isActive) {

                delay(5000)

                val status = StatusController.status.value

                when (status.printerMode) {

                    PrinterMode.LAN -> {
                        if (!status.lanConnected && status.lanIp != null) {
                            connectPrinter()
                        }
                    }

                    PrinterMode.BLUETOOTH -> {
                        if (!status.bluetoothConnected && status.btMac != null) {
                            connectPrinter()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun connectPrinter() {

        val status = StatusController.status.value

        when (status.printerMode) {

            PrinterMode.LAN -> {

                val ip = status.lanIp ?: return

                val client = LanPrinterClient(ip, 9100)

                CoroutineScope(Dispatchers.IO).launch {

                    val ok = client.connect()

                    StatusController.setLanConnected(ok)
                }
            }

            PrinterMode.BLUETOOTH -> {

                val mac = status.btMac ?: return

                val client = BluetoothPrinterClient(mac)

                CoroutineScope(Dispatchers.IO).launch {

                    val ok = client.connect()

                    StatusController.setBluetoothConnected(ok)
                }
            }

            else -> {}
        }
    }

    fun shutdownPrinters() {
        reconnectJob?.cancel()
        reconnectJob = null

        try {
            bluetoothClient.close()
        } catch (_: Exception) {}

        try {
            usbProxy.stop()
        } catch (_: Exception) {}

        Log.d("FiscalAgentApp", "Printers shutdown cleanly")
    }

    private fun saveRawPrint(data: ByteArray) {

        try {

            val dir = File(filesDir, "print_logs")

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val fileName = "print_${System.currentTimeMillis()}.bin"
            val file = File(dir, fileName)

            file.writeBytes(data)

            Log.d("PrintLogger", "Saved: $fileName")

        } catch (e: Exception) {
            Log.e("PrintLogger", "Failed saving print", e)
        }
    }
}