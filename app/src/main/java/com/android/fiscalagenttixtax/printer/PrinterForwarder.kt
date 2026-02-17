package com.android.fiscalagenttixtax.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.os.Build
import androidx.core.content.ContextCompat
import com.android.fiscalagenttixtax.infrastructure.status.StatusController
import com.android.fiscalagenttixtax.printer.bluetooth.BluetoothConnection
import com.android.fiscalagenttixtax.printer.usb.EscPosUsbWriter
import com.android.fiscalagenttixtax.printer.usb.UsbPrinterManager
import com.android.fiscalagenttixtax.queue.PrintQueue
import com.android.fiscalagenttixtax.storage.PrinterConfig
import com.android.fiscalagenttixtax.storage.PrinterType
import java.net.Socket
import java.util.UUID

class PrinterForwarder(
    private val context: Context,
    private val config: PrinterConfig,
    private val printQueue: PrintQueue,
    private val usbManager: UsbPrinterManager? = null,
    private val usbWriter: EscPosUsbWriter? = null
) {

    // =============================
    // Bluetooth
    // =============================

    private var btSocket: BluetoothSocket? = null

    // =============================
    // USB
    // =============================

    private var usbConnection: UsbDeviceConnection? = null
    private var usbDevice: UsbDevice? = null

    fun initUsb() {

        if (usbManager == null) return

        usbDevice = usbManager.findPrinter()
        usbDevice?.let {
            usbConnection = usbManager.open(it)
        }

        StatusController.setUsbPlugged(usbDevice != null)
    }

    // =============================
    // MAIN FORWARD
    // =============================

    fun forward(data: ByteArray) {

        try {

            when (config.type) {

                PrinterType.BLUETOOTH -> {
                    sendBluetooth(data)
                    StatusController.setBluetoothConnected(true)
                }

                PrinterType.LAN -> {
                    sendLan(data)
                    StatusController.setLanConnected(true)
                }

                PrinterType.USB -> {
                    sendUsb(data)
                }
            }

            printQueue.markPrinted(data)

        } catch (e: Exception) {

            when (config.type) {
                PrinterType.BLUETOOTH -> StatusController.setBluetoothConnected(false)
                PrinterType.LAN -> StatusController.setLanConnected(false)
                PrinterType.USB -> StatusController.setUsbPlugged(false)
            }

            e.printStackTrace()
        }
    }

    // =============================
    // BLUETOOTH
    // =============================

    fun connectBluetooth(mac: String) {

        if (!hasBluetoothPermission()) {
            throw SecurityException("BLUETOOTH_CONNECT permission missing")
        }

        val connection = BluetoothConnection(context, mac)
        btSocket = connection.connect()

        StatusController.setBluetoothConnected(btSocket?.isConnected == true)
    }

    private fun sendBluetooth(data: ByteArray) {

        if (btSocket == null || btSocket?.isConnected != true) {
            reconnectBluetooth()
        }

        btSocket!!.outputStream.write(data)
        btSocket!!.outputStream.flush()
    }

    @SuppressLint("MissingPermission")
    private fun reconnectBluetooth() {

        if (!hasBluetoothPermission()) return

        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return

        val device = adapter.getRemoteDevice(config.btMac)

        btSocket = device.createRfcommSocketToServiceRecord(
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
        )

        btSocket?.connect()

        StatusController.setBluetoothConnected(true)
    }

    private fun hasBluetoothPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        } else true
    }

    // =============================
    // LAN
    // =============================

    private fun sendLan(data: ByteArray) {

        Socket(config.lanIp, config.lanPort).use { socket ->
            socket.outputStream.write(data)
            socket.outputStream.flush()
        }
    }

    // =============================
    // USB
    // =============================

    private fun sendUsb(data: ByteArray) {

        if (usbManager == null || usbWriter == null) return

        if (usbDevice == null || usbConnection == null) {
            initUsb()
        }

        if (usbDevice == null || usbConnection == null) return

        usbWriter.write(
            connection = usbConnection!!,
            device = usbDevice!!,
            data = data
        )
    }
}