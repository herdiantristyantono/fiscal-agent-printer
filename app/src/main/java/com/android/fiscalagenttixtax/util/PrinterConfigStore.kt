package com.android.fiscalagenttixtax.util

import android.content.Context

object PrinterConfigStore {

    private const val PREF = "printer_config"

    private const val KEY_MAC = "mac"
    private const val KEY_LAN_IP = "lan_ip"

    // ============================
    // 🔵 BLUETOOTH
    // ============================

    fun saveMac(context: Context, mac: String) {

        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MAC, mac)
            .apply()
    }

    fun loadMac(context: Context): String? {

        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_MAC, null)
    }

    // ============================
    // 🌐 LAN
    // ============================

    fun saveLanIp(context: Context, ip: String) {

        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAN_IP, ip)
            .apply()
    }

    fun loadLanIp(context: Context): String? {

        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_LAN_IP, null)
    }
}