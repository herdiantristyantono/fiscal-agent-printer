package com.android.fiscalagenttixtax.printer.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.UUID

class BluetoothConnection(
    private val context: Context,
    private val printerMac: String
) {

    private val adapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()

    private val uuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var socket: BluetoothSocket? = null

    private fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun connect(): BluetoothSocket? {

        if (!hasConnectPermission()) {
            Log.e("BT", "Missing BLUETOOTH_CONNECT permission")
            return null
        }

        if (adapter == null) {
            Log.e("BT", "Bluetooth not supported")
            return null
        }

        return try {

            val device: BluetoothDevice =
                adapter.bondedDevices.firstOrNull {
                    it.address == printerMac
                } ?: run {
                    Log.e("BT", "Printer not paired: $printerMac")
                    return null
                }

            adapter.cancelDiscovery()

            socket =
                device.createRfcommSocketToServiceRecord(uuid)

            socket?.connect()

            Log.d("BT", "Connected to printer")

            socket

        } catch (e: SecurityException) {

            Log.e("BT", "Permission error", e)
            null

        } catch (e: Exception) {

            Log.e("BT", "Connection failed", e)
            null
        }
    }

    fun getSocket(): BluetoothSocket? = socket

    fun close() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
    }
}