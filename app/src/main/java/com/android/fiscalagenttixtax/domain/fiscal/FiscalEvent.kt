package com.android.fiscalagenttixtax.domain.fiscal

data class FiscalEvent(
    val id: String,
    val type: FiscalEventType,
    val source: FiscalEventSource,
    val amount: Long,
    val receiptHash: String,
    val previousHash: String?,
    val chainHash: String,
    val receiptRef: String? = null,
    val referenceEventId: String? = null,
    val createdAt: Long,
    val refEventId: String? = null,
    val monotonicTime: Long,
    val sequence: Long
)