package com.android.fiscalagenttixtax.queue

import android.annotation.SuppressLint
import java.util.LinkedList

import android.content.Context
import java.util.concurrent.ConcurrentLinkedQueue

class PrintQueue private constructor(
    private val context: Context
) {

    private val queue = ConcurrentLinkedQueue<ByteArray>()

    fun enqueue(data: ByteArray) {
        queue.offer(data)
    }

    fun dequeue(): ByteArray? {
        return queue.poll()
    }

    fun isEmpty(): Boolean = queue.isEmpty()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: PrintQueue? = null

        fun getInstance(context: Context): PrintQueue {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrintQueue(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }
    }

    fun markPrinted(data: ByteArray) {
        // optional: remove from queue, persist state, metrics
    }
}