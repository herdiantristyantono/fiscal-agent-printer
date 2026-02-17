package com.android.fiscalagenttixtax.printer.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.android.fiscalagenttixtax.infrastructure.status.StatusController
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinterClient(
    private val macAddress: String
) {

    private val TAG = "BluetoothPrinterClient"

    private val uuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var socket: BluetoothSocket? = null
    private var output: OutputStream? = null

    @SuppressLint("MissingPermission")
    fun connect(): Boolean {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: return false

            val device = adapter.getRemoteDevice(macAddress)

            socket = device.createRfcommSocketToServiceRecord(uuid)

            adapter.cancelDiscovery()

            socket!!.connect()

            output = socket!!.outputStream

            Log.d(TAG, "Bluetooth connected to printer")

            StatusController.setBluetoothConnected(true)

            listenDisconnect()

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth connect failed", e)
            return false
        }
    }

    fun write(data: ByteArray) {

        try {

            if (socket == null || !socket!!.isConnected) {
                connect()
            }

            output?.write(data)
            output?.flush()

        } catch (e: Exception) {
            Log.e(TAG, "Write failed", e)
            StatusController.setBluetoothConnected(false)

            close()
        }
    }

    fun close() {
        try {
            output?.close()
            socket?.close()
        } catch (_: Exception) {}
    }

    private fun listenDisconnect() {

        try {
            socket?.inputStream?.read()
        } catch (e: Exception) {
            Log.e(TAG, "Printer disconnected")
        }

        StatusController.setBluetoothConnected(false)
        socket?.close()
    }

    fun isConnected(): Boolean = socket?.isConnected == true
}