package com.android.fiscalagenttixtax.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.fiscalagenttixtax.infrastructure.status.AgentStatus
import com.android.fiscalagenttixtax.infrastructure.status.PrinterMode
import com.android.fiscalagenttixtax.util.PrinterConfigStore
import androidx.compose.foundation.lazy.items
import com.android.fiscalagenttixtax.infrastructure.bluetooth.BluetoothScanner
import com.android.fiscalagenttixtax.ui.bluetooth.BluetoothViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.fiscalagenttixtax.permission.rememberBluetoothPermissionRequester

@SuppressLint("MissingPermission")
@Composable
fun PrinterSetupScreen(
    context: Context,
    status: AgentStatus,
    onSelectBluetooth: () -> Unit,
    onSelectLan: () -> Unit,
//    onConnectBluetooth: (String) -> Unit,
//    onDisconnectBluetooth: () -> Unit,
    onConnectLan: (String) -> Unit,
    onDisconnectLan: () -> Unit
) {

//    var mac by rememberSaveable {
//        mutableStateOf(PrinterConfigStore.loadMac(context) ?: "")
//    }

    var lanIp by rememberSaveable {
        mutableStateOf(PrinterConfigStore.loadLanIp(context) ?: "")
    }

//    var isScanning by remember { mutableStateOf(false) }
//    var connectingAddress by remember { mutableStateOf<String?>(null) }

//    val scanner = remember {
//        BluetoothScanner(context)
//    }

//    val devices by scanner.devices.collectAsState()

    val viewModel: BluetoothViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val requestPermissionAndScan =
        rememberBluetoothPermissionRequester {
            viewModel.toggleScan()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Printer Connection", fontSize = 22.sp)

        // ================= MODE SWITCH =================

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            ModeButton(
                text = "Use LAN",
                selected = status.printerMode == PrinterMode.LAN,
                onClick = onSelectLan
            )

            ModeButton(
                text = "Use Bluetooth",
                selected = status.printerMode == PrinterMode.BLUETOOTH,
                onClick = onSelectBluetooth
            )
        }

        // ================= LAN =================

        if (status.printerMode == PrinterMode.LAN) {

            OutlinedTextField(
                value = lanIp,
                onValueChange = { lanIp = it },
                label = { Text("Printer IP Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (lanIp.isNotBlank()) {
                        PrinterConfigStore.saveLanIp(context, lanIp)
                        if (status.lanConnected)
                            onDisconnectLan()
                        else
                            onConnectLan(lanIp)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                    if (status.lanConnected) Color.Red
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (status.lanConnected)
                        "Disconnect LAN Printer"
                    else
                        "Connect LAN Printer"
                )
            }
        }

        // ================= BLUETOOTH =================

//        if (status.printerMode == PrinterMode.BLUETOOTH) {
//
//            OutlinedTextField(
//                value = mac,
//                onValueChange = { mac = it },
//                label = { Text("Printer MAC Address") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Button(
//                onClick = {
//                    if (mac.isNotBlank()) {
//                        PrinterConfigStore.saveMac(context, mac)
//                        if (status.bluetoothConnected)
//                            onDisconnectBluetooth()
//                        else
//                            onConnectBluetooth(mac)
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor =
//                    if (status.bluetoothConnected) Color.Red
//                    else MaterialTheme.colorScheme.primary
//                )
//            ) {
//                Text(
//                    if (status.bluetoothConnected)
//                        "Disconnect Bluetooth Printer"
//                    else
//                        "Connect Bluetooth Printer"
//                )
//            }
//        }

        if (status.printerMode == PrinterMode.BLUETOOTH) {

            Text("Bluetooth Devices", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { requestPermissionAndScan() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isScanning) "Stop Scan" else "Scan Devices")
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {

                when {

                    !uiState.isScanning && uiState.devices.isEmpty() -> {
                        Text(
                            "Press 'Scan Devices' to search nearby Bluetooth printers.",
                            color = Color.Gray
                        )
                    }

                    uiState.isScanning && uiState.devices.isEmpty() -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Scanning...")
                        }
                    }

                    else -> {

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.devices) { device ->

                                val isConnected =
                                    uiState.connectedAddress == device.address

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.connect(device.address)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                        if (isConnected)
                                            Color(0xFFDFF6DD)
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {

                                    Column(Modifier.padding(16.dp)) {

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {

                                            Text(
                                                device.name ?: "Unknown Device",
                                                fontSize = 16.sp
                                            )

                                            Text(
                                                "${device.rssi} dBm",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Text(
                                            device.address,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )

                                        if (uiState.connectingAddress == device.address) {
                                            Spacer(Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text("Connecting...")
                                            }
                                        }

                                        if (isConnected) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                "Connected ✓",
                                                color = Color(0xFF2ECC71)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.connectedAddress != null) {

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Disconnect")
                }
            }
        }

        // ================= STATUS =================

        val connected =
            status.lanConnected || status.bluetoothConnected

        val label =
            if (status.printerMode == PrinterMode.LAN)
                "LAN Printer"
            else
                "Bluetooth Printer"

        val color =
            if (connected) Color(0xFF2ECC71)
            else Color(0xFFE74C3C)

        val text =
            if (connected) "ONLINE" else "OFFLINE"

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Text("$label : $text")
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (selected)
            ButtonDefaults.buttonColors(containerColor = Color(0xFF6A4FB3))
        else
            ButtonDefaults.buttonColors(containerColor = Color.LightGray)
    ) {
        Text(
            text,
            color = if (selected) Color.White else Color.Black
        )
    }
}