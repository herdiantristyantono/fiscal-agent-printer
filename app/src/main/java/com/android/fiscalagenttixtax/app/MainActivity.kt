package com.android.fiscalagenttixtax.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.fiscalagenttixtax.infrastructure.status.StatusController
import com.android.fiscalagenttixtax.ui.PrinterSetupScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.android.fiscalagenttixtax.service.PrinterForegroundService

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivityApp"
    private var started = false
    private var permissionLaunched = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Main Activity started")
        permissionLaunched =
            savedInstanceState?.getBoolean("permissionLaunched") ?: false

        checkAndRequestBluetoothPermission()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("permissionLaunched", permissionLaunched)
    }

    // ----------------------------------------------------
    // 🔐 PERMISSIONS
    // ----------------------------------------------------

    private fun checkAndRequestBluetoothPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            startOnce()
            return
        }

        val scanGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

        val connectGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        if (scanGranted && connectGranted) {
            startOnce()
            return
        }

        if (!permissionLaunched) {
            permissionLaunched = true
            bluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            permissionLaunched = false

            val granted = permissions.values.all { it }

            if (granted) {
                startOnce()
            }
        }


    // ----------------------------------------------------
    // 🚀 START SERVICES + UI
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startOnce() {

        if (started) return
        started = true

        Log.d(TAG, "Starting fiscal services")

        // ▶ background services
        val intent = Intent(this, PrinterForegroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        setupBluetoothName()
        makeDiscoverable()

        // ▶ UI
        setContent {

            val status by StatusController.status.collectAsState()

            PrinterSetupScreen(
                context = applicationContext,
                status = status,

                // ================= MODE =================
                onSelectLan = {
                    StatusController.selectLan()
                },

                onSelectBluetooth = {
                    StatusController.selectBluetooth()
                },

                // ================= LAN =================
                onConnectLan = { ip ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val ok = StatusController.connectLanPrinter(ip)
                        StatusController.setLanConnected(ok)
                    }
                },

                onDisconnectLan = {
                    StatusController.disconnectLan()
                },

                // ================= BLUETOOTH =================
//                onConnectBluetooth = { mac ->
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        val client = BluetoothPrinterClient(mac)
//                        val ok = client.connect()
//                        StatusController.setBluetoothConnected(ok)
//                    }
//                },

//                onDisconnectBluetooth = {
//                    StatusController.disconnectBluetooth()
//                }
            )
        }
    }

    // ----------------------------------------------------
    // 📡 BLUETOOTH CONFIG
    // ----------------------------------------------------

    private fun setupBluetoothName() {

        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    adapter.name = "FiscalAgentPrinter"
                } catch (e: SecurityException) {
                    Log.e(TAG, "No permission to set Bluetooth name", e)
                }
            }
        } else {
            adapter.name = "FiscalAgentPrinter"
        }
    }

    private fun makeDiscoverable() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                300
            )
        }

        startActivity(intent)
    }
}