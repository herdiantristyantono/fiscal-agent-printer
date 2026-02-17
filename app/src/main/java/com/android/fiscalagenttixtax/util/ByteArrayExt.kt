package com.android.fiscalagenttixtax.util

fun ByteArray.toHex(): String =
    joinToString("") { "%02x".format(it) }