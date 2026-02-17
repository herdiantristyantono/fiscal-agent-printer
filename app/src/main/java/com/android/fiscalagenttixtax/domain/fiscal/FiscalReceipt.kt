package com.android.fiscalagenttixtax.domain.fiscal

data class FiscalReceipt(
    val items: List<String>,
    val totalAmount: Long,
    val rawData: ByteArray,
    val reference: String? = null
)