package com.android.fiscalagenttixtax.infrastructure.sequence

import java.util.concurrent.atomic.AtomicLong

class SequenceGenerator {

    private val counter = AtomicLong(0)

    fun next(): Long = counter.incrementAndGet()
}