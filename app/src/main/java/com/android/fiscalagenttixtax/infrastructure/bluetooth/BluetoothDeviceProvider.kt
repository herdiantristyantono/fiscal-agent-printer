package com.android.fiscalagenttixtax.infrastructure.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

object BluetoothDeviceProvider {

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {

        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: return emptyList()

        return adapter.bondedDevices.toList()
    }
}