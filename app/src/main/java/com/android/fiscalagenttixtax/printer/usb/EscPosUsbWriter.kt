package com.android.fiscalagenttixtax.printer.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection

class EscPosUsbWriter {

    fun write(
        connection: UsbDeviceConnection,
        device: UsbDevice,
        data: ByteArray
    ) {

        val intf = device.getInterface(0)
        val endpoint = intf.getEndpoint(0)

        connection.claimInterface(intf, true)

        connection.bulkTransfer(
            endpoint,
            data,
            data.size,
            3000
        )
    }
}