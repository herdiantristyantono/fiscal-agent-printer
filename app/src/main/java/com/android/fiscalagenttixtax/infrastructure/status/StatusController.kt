package com.android.fiscalagenttixtax.infrastructure.status

import com.android.fiscalagenttixtax.printer.bluetooth.BluetoothPrinterClient
import com.android.fiscalagenttixtax.printer.lan.LanPrintServer
import com.android.fiscalagenttixtax.printer.lan.LanPrinterClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object StatusController {

    val _status = MutableStateFlow(AgentStatus())
    val status: StateFlow<AgentStatus> = _status

    private var lanServer: LanPrintServer? = null
    private var lanClient: LanPrinterClient? = null
    private var bluetoothClient: BluetoothPrinterClient? = null

    // ------------------------------------------------
    // 📡 MODE SELECT (UI calls these)
    // ------------------------------------------------

    fun selectLan() {
        _status.update {
            it.copy(
                printerMode = PrinterMode.LAN,
                bluetoothConnected = false // force release
            )
        }
    }

    fun selectBluetooth() {
        _status.update {
            it.copy(
                printerMode = PrinterMode.BLUETOOTH,
                lanConnected = false // force release
            )
        }
    }

    fun selectUsb() {
        _status.value = _status.value.copy(
            printerMode = PrinterMode.USB
        )
    }

    fun resetMode() {
        _status.value = AgentStatus()
    }

    // ------------------------------------------------
    // 🔵 BLUETOOTH STATUS
    // ------------------------------------------------

    fun setBluetoothConnected(connected: Boolean) {
        _status.value = _status.value.copy(
            bluetoothConnected = connected
        )
    }

    // ------------------------------------------------
    // 🌐 LAN STATUS + SERVER
    // ------------------------------------------------

    fun connectLanPrinter(ip: String): Boolean {

        return try {

            if (lanServer == null) {

                lanServer = LanPrintServer(9100) { data ->

                    setPosConnected(true)
                }

                lanServer!!.start()
            }

            _status.value = _status.value.copy(
                lanConnected = true
            )

            true

        } catch (e: Exception) {
            false
        }
    }

    fun setLanConnected(connected: Boolean) {
        _status.value = _status.value.copy(
            lanConnected = connected
        )
    }

    // ------------------------------------------------
    // 📦 POS
    // ------------------------------------------------

    fun setPosConnected(connected: Boolean) {
        _status.value = _status.value.copy(
            posConnected = connected
        )
    }

    // ------------------------------------------------
    // 🔌 USB
    // ------------------------------------------------

    fun setUsbPlugged(plugged: Boolean) {
        _status.value = _status.value.copy(
            usbPlugged = plugged
        )
    }

    fun disconnectLan() {

        try {
            lanClient?.disconnect()   // pastikan class punya fungsi close/disconnect
            lanClient = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _status.update {
            it.copy(lanConnected = false)
        }
    }

    fun disconnectBluetooth() {

        try {
            bluetoothClient?.close()
            bluetoothClient = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _status.update {
            it.copy(bluetoothConnected = false)
        }
    }
}