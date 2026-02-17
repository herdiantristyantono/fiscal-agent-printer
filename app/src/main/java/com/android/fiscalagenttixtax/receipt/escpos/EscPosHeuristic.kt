package com.android.fiscalagenttixtax.receipt.escpos

import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventType
import java.nio.charset.Charset
import java.util.Locale

object EscPosHeuristic {

    private val refundKeywords = listOf(
        "REFUND",
        "RETUR",
        "VOID",
        "BATAL"
    )

    fun detectType(text: String): FiscalEventType {
        val upper = text.uppercase(Locale.getDefault())

        return when {
            refundKeywords.any { upper.contains(it) } ->
                FiscalEventType.REFUND
            else ->
                FiscalEventType.SALE
        }
    }

    fun extractTotal(text: String): Long {
        // heuristic: last number with 2 decimal or currency
        val regex = Regex("""(\d+[.,]\d{2})""")
        val matches = regex.findAll(text).toList()
        val last = matches.lastOrNull()?.value ?: return 0L

        return last
            .replace(",", "")
            .replace(".", "")
            .toLong()
    }

    fun decode(bytes: ByteArray): String {
        return bytes.toString(Charset.forName("UTF-8"))
    }
}