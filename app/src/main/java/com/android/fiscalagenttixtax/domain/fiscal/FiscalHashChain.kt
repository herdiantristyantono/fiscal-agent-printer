package com.android.fiscalagenttixtax.domain.fiscal

import com.android.fiscalagenttixtax.security.ReceiptHasher
import com.android.fiscalagenttixtax.util.toHex
import java.security.MessageDigest

object FiscalHashChain {

    fun compute(
        previousHash: String?,
        receiptRaw: ByteArray,
        event: FiscalEvent
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")

        previousHash?.let { digest.update(it.toByteArray()) }

        digest.update(receiptRaw)
        digest.update(event.type.name.toByteArray())
        digest.update(event.amount.toString().toByteArray())
        digest.update(event.monotonicTime.toString().toByteArray())
        digest.update(event.sequence.toString().toByteArray())

        return digest.digest().toHex()
    }
}