package com.android.fiscalagenttixtax.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PrinterConfigRepository {

    private const val PREF_NAME = "fiscal_printer_config"

    fun load(context: Context): PrinterConfig {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val type = prefs.getString("type", null)
            ?: throw IllegalStateException("Printer not configured")

        return when (PrinterType.valueOf(type)) {

            PrinterType.BLUETOOTH -> PrinterConfig(
                type = PrinterType.BLUETOOTH,
                btMac = prefs.getString("bt_mac", null)
                    ?: throw IllegalStateException("Bluetooth MAC missing")
            )

            PrinterType.LAN -> PrinterConfig(
                type = PrinterType.LAN,
                lanIp = prefs.getString("lan_ip", null)
                    ?: throw IllegalStateException("LAN IP missing"),
                lanPort = prefs.getInt("lan_port", 9100)
            )

            PrinterType.USB -> PrinterConfig(
                type = PrinterType.USB
                // USB biasanya gak butuh parameter
            )
        }
    }

    fun saveBluetoothPrinter(
        context: Context,
        macAddress: String
    ) {
        saveConfig(
            context,
            PrinterConfig(
                type = PrinterType.BLUETOOTH,
                btMac = macAddress
            )
        )
    }

    fun saveLanPrinter(
        context: Context,
        ip: String,
        port: Int
    ) {
        saveConfig(
            context,
            PrinterConfig(
                type = PrinterType.LAN,
                lanIp = ip,
                lanPort = port
            )
        )
    }

    private fun saveConfig(
        context: Context,
        config: PrinterConfig
    ) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "fiscal_printer_config",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(prefs.edit()) {
            putString("type", config.type.name)

            when (config.type) {

                PrinterType.BLUETOOTH -> {
                    putString("bt_mac", config.btMac)
                    remove("lan_ip")
                    remove("lan_port")
                }

                PrinterType.LAN -> {
                    putString("lan_ip", config.lanIp)
                    putInt("lan_port", config.lanPort)
                    remove("bt_mac")
                }

                PrinterType.USB -> {
                    // bersihkan semua karena USB plug & play
                    remove("bt_mac")
                    remove("lan_ip")
                    remove("lan_port")
                }
            }
            apply()
        }
    }
}