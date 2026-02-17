package com.android.fiscalagenttixtax.data.fiscal

import com.android.fiscalagenttixtax.domain.fiscal.FiscalEvent
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventSource
import com.android.fiscalagenttixtax.domain.fiscal.FiscalEventType

object FiscalEventMapper {

    fun toDomain(entity: FiscalEventEntity): FiscalEvent {
        return FiscalEvent(
            id = entity.id,
            type = FiscalEventType.valueOf(entity.type),
            source = FiscalEventSource.valueOf(entity.source),
            amount = entity.amount,
            receiptHash = entity.receiptHash,
            previousHash = entity.previousHash,
            chainHash = entity.chainHash,
            receiptRef = entity.receiptRef.toString(),
            referenceEventId = entity.referenceEventId,
            createdAt = entity.createdAt,
            refEventId = entity.refEventId,
            monotonicTime = entity.monotonicTime,
            sequence = entity.sequence
        )
    }

    fun toEntity(
        event: FiscalEvent,
        receiptRef: String?
    ): FiscalEventEntity {
        return FiscalEventEntity(
            id = event.id,
            type = event.type.name,
            source = event.source.name,
            amount = event.amount,
            receiptHash = event.receiptHash,
            previousHash = event.previousHash,
            chainHash = event.chainHash,
            receiptRef = receiptRef,
            referenceEventId = event.referenceEventId,
            createdAt = event.createdAt,
            refEventId = event.refEventId,
            monotonicTime = event.monotonicTime,
            sequence = event.sequence
        )
    }
}