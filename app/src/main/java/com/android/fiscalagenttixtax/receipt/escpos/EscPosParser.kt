package com.android.fiscalagenttixtax.receipt.escpos

import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventType
import com.android.fiscalagenttixtax.domain.fiscal.FiscalReceipt

class EscPosParser {

    private fun extractReference(text: String): String? {
        val patterns = listOf(
            Regex("REF[: ]+(\\w+)", RegexOption.IGNORE_CASE),
            Regex("RECEIPT NO[: ]+(\\w+)", RegexOption.IGNORE_CASE),
            Regex("INVOICE[: ]+(\\w+)", RegexOption.IGNORE_CASE)
        )

        return patterns.firstNotNullOfOrNull {
            it.find(text)?.groupValues?.get(1)
        }
    }

    fun parse(raw: ByteArray): Pair<FiscalReceipt, FiscalEventType> {
        val text = EscPosHeuristic.decode(raw)
        val normalizedText = text
            .uppercase()
            .replace(Regex("\\s+"), " ")
        val type = when {
            normalizedText.contains("REFUND")
                    || normalizedText.contains("RETUR") ->
                FiscalEventType.REFUND

            normalizedText.contains("VOID")
                    || normalizedText.contains("BATAL") ->
                FiscalEventType.VOID

            else ->
                FiscalEventType.SALE
        }
        val total = EscPosHeuristic.extractTotal(text)
        val reference = extractReference(normalizedText)

        val receipt = FiscalReceipt(
            items = text.lines(),
            totalAmount = total,
            rawData = raw,
            reference = reference
        )

        return receipt to type
    }
}