package com.android.fiscalagenttixtax.printer.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager

class UsbPrinterManager(
    private val context: Context
) {

    private val usbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun findPrinter(): UsbDevice? {
        return usbManager.deviceList.values.firstOrNull {
            it.interfaceCount > 0
        }
    }

    fun open(device: UsbDevice): UsbDeviceConnection? {
        if (!usbManager.hasPermission(device)) return null
        return usbManager.openDevice(device)
    }
}