package com.android.fiscalagenttixtax.data.fiscal

import com.android.fiscalagenttixtax.domain.fiscal.FiscalEvent

class FiscalEventRepository(
    private val dao: FiscalEventDao
) {

    suspend fun save(
        event: FiscalEvent,
        receiptRef: String?
    ) {
        dao.insert(
            FiscalEventMapper.toEntity(event, receiptRef)
        )
    }

    suspend fun getLastHash(): String? {
        return dao.getLastChainHash()
    }

    suspend fun getAll(): List<FiscalEvent> {
        return dao.getAll().map {
            FiscalEventMapper.toDomain(it)
        }
    }

    fun findSaleByReference(ref: String): FiscalEvent? {
        return dao.findSaleByReceiptRef(ref)
            ?.let { FiscalEventMapper.toDomain(it) }
    }

    suspend fun findByReceiptHash(hash: String): FiscalEvent? {
        return dao.findByReceiptHash(hash)
            ?.let { FiscalEventMapper.toDomain(it) }
    }

    suspend fun getLastEventTime(): Long? {
        return dao.getLastEventTime()
    }
}