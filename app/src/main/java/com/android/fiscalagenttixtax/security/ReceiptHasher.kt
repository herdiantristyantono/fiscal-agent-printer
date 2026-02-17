package com.android.fiscalagenttixtax.security

import java.security.MessageDigest

object ReceiptHasher {

    fun sha256(input: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input)
            .joinToString("") { "%02x".format(it) }
    }

    fun sha256(input: String): String =
        sha256(input.toByteArray(Charsets.UTF_8))
}