package com.android.fiscalagenttixtax.printer

import android.util.Log
import com.android.fiscalagenttixtax.infrastructure.PrintLogger
import com.android.fiscalagenttixtax.printer.bluetooth.BluetoothPrinterClient

class LanToBluetoothForwarder(
    private val bluetooth: BluetoothPrinterClient
) {

    private val TAG = "LanToBluetoothForwarder"

    fun forward(data: ByteArray) {

        Log.d(TAG, "Forward ${data.size} bytes to Bluetooth printer")

        PrintLogger.logRaw(data)

        bluetooth.write(data)
    }
}