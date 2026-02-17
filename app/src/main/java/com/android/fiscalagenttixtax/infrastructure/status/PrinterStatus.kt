package com.android.fiscalagenttixtax.infrastructure.status

enum class PrinterMode {
    LAN,
    BLUETOOTH,
    USB,
    NONE
}

data class AgentStatus(
    val printerMode: PrinterMode = PrinterMode.NONE,

    val lanIp: String? = null,
    val btMac: String? = null,

    val lanConnected: Boolean = false,
    val bluetoothConnected: Boolean = false,

    val usbPlugged: Boolean = false,
    val posConnected: Boolean = false
)