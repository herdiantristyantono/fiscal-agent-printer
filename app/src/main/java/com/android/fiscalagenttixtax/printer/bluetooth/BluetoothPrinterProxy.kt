package com.android.fiscalagenttixtax.printer.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.util.UUID

class BluetoothPrinterProxy(
    private val macAddress: String
) {

    private val TAG = "BluetoothPrinterProxy"

    private var socket: BluetoothSocket? = null

    private val uuid =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP

    @SuppressLint("MissingPermission")
    fun connect(): Boolean {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(macAddress)

            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()

            Log.d(TAG, "Bluetooth connected to printer")

            true

        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth connect failed", e)
            false
        }
    }

    fun send(data: ByteArray) {

        try {
            if (socket == null || !socket!!.isConnected) {
                connect()
            }

            socket!!.outputStream.write(data)
            socket!!.outputStream.flush()

        } catch (e: Exception) {
            Log.e(TAG, "Send failed", e)
            connect() // auto reconnect
        }
    }

    fun close() {
        socket?.close()
    }
}