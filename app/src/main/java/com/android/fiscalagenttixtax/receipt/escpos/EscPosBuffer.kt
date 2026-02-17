package com.android.fiscalagentixtax.receipt.escpos

import com.android.fiscalagenttixtax.receipt.escpos.EscPosCommands
import java.io.ByteArrayOutputStream

class EscPosBuffer {

    private val stream = ByteArrayOutputStream()

    fun append(chunk: ByteArray) {
        stream.write(chunk)
    }

    fun isReceiptEnd(chunk: ByteArray): Boolean {
        return containsSubArray(chunk, EscPosCommands.CUT)
    }

    fun flush(): ByteArray {
        val data = stream.toByteArray()
        stream.reset()
        return data
    }

    private fun containsSubArray(source: ByteArray, target: ByteArray): Boolean {
        if (source.size < target.size) return false

        for (i in 0..source.size - target.size) {
            if (source.sliceArray(i until i + target.size)
                    .contentEquals(target)
            ) {
                return true
            }
        }
        return false
    }
}