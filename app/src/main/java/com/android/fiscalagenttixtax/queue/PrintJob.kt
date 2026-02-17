package com.android.fiscalagenttixtax.queue

data class PrintJob(
    val payload: ByteArray,
    val retryCount: Int = 0
)