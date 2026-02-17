package com.android.fiscalagenttixtax.infrastructure.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ScannedDevice(
    val name: String?,
    val address: String,
    val rssi: Short = 0,
    val bonded: Boolean = false
)

@SuppressLint("MissingPermission")
class BluetoothScanner(private val context: Context) {

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    private val _devices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val devices: StateFlow<List<ScannedDevice>> = _devices

    private val receiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {

            when (intent?.action) {

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    val rssi =
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, 0)

                    device?.let {
                        val newDevice = ScannedDevice(
                            name = it.name,
                            address = it.address,
                            rssi = rssi,
                            bonded = it.bondState == BluetoothDevice.BOND_BONDED
                        )

                        _devices.value =
                            (_devices.value + newDevice)
                                .distinctBy { d -> d.address }
                    }
                }
            }
        }
    }

    fun startScan() {

        _devices.value = emptyList()

        context.registerReceiver(
            receiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        adapter?.startDiscovery()
    }

    fun stopScan() {
        adapter?.cancelDiscovery()
        context.unregisterReceiver(receiver)
    }
}