package com.android.fiscalagenttixtax.storage

data class PrinterConfig(
    val type: PrinterType,
    val btMac: String? = null,
    val lanIp: String? = null,
    val lanPort: Int = 9100
)