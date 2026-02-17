package com.android.fiscalagenttixtax.infrastructure

import android.util.Log

object PrintLogger {

    private const val TAG = "PRINT_DATA"

    fun logRaw(data: ByteArray) {

        // 🔤 Try text
        val text = runCatching {
            String(data, Charsets.UTF_8)
        }.getOrNull()

        if (!text.isNullOrBlank()) {
            Log.d(TAG, "TEXT >>> $text")
        }

        // 🔢 Hex dump (safe for ESC/POS)
        val hex = data.joinToString(" ") {
            "%02X".format(it)
        }

        Log.d(TAG, "HEX >>> $hex")
        PrintStreamController.append(data)
    }
}