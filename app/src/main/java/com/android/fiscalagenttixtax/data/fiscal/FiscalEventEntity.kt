package com.android.fiscalagenttixtax.data.fiscal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fiscal_events")
data class FiscalEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val source: String,
    val amount: Long,
    val receiptHash: String,
    val previousHash: String?,
    val chainHash: String,
    val receiptRef: String?,      // 👈 DB-only
    val referenceEventId: String?,
    val createdAt: Long,
    val refEventId: String?,
    val monotonicTime: Long,
    val sequence: Long
)