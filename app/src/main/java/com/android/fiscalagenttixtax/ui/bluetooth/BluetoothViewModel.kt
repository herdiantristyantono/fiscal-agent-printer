package com.android.fiscalagenttixtax.ui.bluetooth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.fiscalagenttixtax.infrastructure.bluetooth.*
import com.android.fiscalagenttixtax.printer.bluetooth.BluetoothPrinterClient
import com.android.fiscalagenttixtax.util.PrinterConfigStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BluetoothUiState(
    val devices: List<ScannedDevice> = emptyList(),
    val isScanning: Boolean = false,
    val connectingAddress: String? = null,
    val connectedAddress: String? = null
)

class BluetoothViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()
    private val scanner = BluetoothScanner(context)

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    init {
        observeScanner()
        autoReconnect()
    }

    private fun observeScanner() {
        viewModelScope.launch {
            scanner.devices.collect { devices ->
                _uiState.update {
                    it.copy(devices = devices)
                }
            }
        }
    }

    // ================= SCAN =================

    fun toggleScan() {
        val scanning = _uiState.value.isScanning

        if (scanning) {
            scanner.stopScan()
        } else {
            scanner.startScan()
        }

        _uiState.update {
            it.copy(isScanning = !scanning)
        }
    }

    // ================= CONNECT =================

    fun connect(address: String) {

        // 🔒 Auto stop scan saat connect
        if (_uiState.value.isScanning) {
            scanner.stopScan()
        }

        _uiState.update {
            it.copy(
                connectingAddress = address,
                isScanning = false
            )
        }

        viewModelScope.launch {

            val client = BluetoothPrinterClient(address)
            val ok = client.connect()

            if (ok) {
                PrinterConfigStore.saveMac(context, address)
            }

            _uiState.update {
                it.copy(
                    connectingAddress = null,
                    connectedAddress = if (ok) address else null
                )
            }
        }
    }

    fun disconnect() {
        _uiState.update {
            it.copy(connectedAddress = null)
        }
    }

    // ================= AUTO RECONNECT =================

    private fun autoReconnect() {

        val lastMac = PrinterConfigStore.loadMac(context) ?: return

        viewModelScope.launch {

            val client = BluetoothPrinterClient(lastMac)
            val ok = client.connect()

            if (ok) {
                _uiState.update {
                    it.copy(connectedAddress = lastMac)
                }
            }
        }
    }
}