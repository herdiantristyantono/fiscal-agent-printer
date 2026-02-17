package com.android.fiscalagenttixtax.data.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FiscalEventDao {
    @Insert
    suspend fun insert(event: FiscalEventEntity)

    @Query("SELECT * FROM fiscal_events")
    suspend fun getAll(): List<FiscalEventEntity>

    @Query("SELECT chainHash FROM fiscal_events ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastChainHash(): String?

    @Query("""
    SELECT * FROM fiscal_events
    WHERE type = 'SALE'
    AND receiptRef = :ref
    LIMIT 1
""")
    fun findSaleByReceiptRef(ref: String): FiscalEventEntity?

    @Query("""
    SELECT * FROM fiscal_events
    WHERE receiptHash = :hash
    ORDER BY createdAt DESC
    LIMIT 1
""")
    suspend fun findByReceiptHash(hash: String): FiscalEventEntity?

    @Query("""
    SELECT createdAt FROM fiscal_events
    ORDER BY createdAt DESC
    LIMIT 1
""")
    suspend fun getLastEventTime(): Long?
}