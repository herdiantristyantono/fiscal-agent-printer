package com.android.fiscalagenttixtax.infrastructure

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PrintStreamController {

    private val _stream = MutableStateFlow("")
    val stream = _stream.asStateFlow()

    fun append(data: ByteArray) {

        val text = runCatching {
            String(data, Charsets.UTF_8)
        }.getOrNull() ?: return

        _stream.value += text
    }

    fun clear() {
        _stream.value = ""
    }
}