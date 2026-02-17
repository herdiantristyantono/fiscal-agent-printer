package com.android.fiscalagenttixtax.receipt.escpos

object EscPosCommands {
    val CUT = byteArrayOf(0x1D, 0x56)
    val FEED = byteArrayOf(0x0A)
    val ESC = 0x1B.toByte()
}