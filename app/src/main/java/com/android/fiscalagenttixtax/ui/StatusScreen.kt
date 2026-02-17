package com.android.fiscalagenttixtax.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.fiscalagenttixtax.infrastructure.status.AgentStatus
import com.android.fiscalagenttixtax.infrastructure.status.PrinterMode
import com.android.fiscalagenttixtax.infrastructure.status.StatusController

@Composable
fun StatusScreen(status: AgentStatus) {

    var lanIp by remember { mutableStateOf("") }
    var btMac by remember { mutableStateOf("") }

    Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Printer Connection", fontSize = 22.sp)

        // ========= MODE PICKER =========

        Row {

            Button(
                onClick = {
                    StatusController.selectLan()
                },
                enabled = status.printerMode != PrinterMode.BLUETOOTH
            ) {
                Text("Use LAN")
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    StatusController.selectBluetooth()
                },
                enabled = status.printerMode != PrinterMode.LAN
            ) {
                Text("Use Bluetooth")
            }
        }

        // ========= LAN INPUT =========

        if (status.printerMode == PrinterMode.LAN) {

            OutlinedTextField(
                value = lanIp,
                onValueChange = { lanIp = it },
                label = { Text("Printer IP Address") },
                modifier = Modifier.fillMaxWidth()
            )

            StatusRow("LAN Printer", status.lanConnected)

            if (status.lanConnected) {
                Text("Connected to: ${status.lanIp}", color = Color.Green)
            }
        }

        // ========= BLUETOOTH INPUT =========

        if (status.printerMode == PrinterMode.BLUETOOTH) {

            OutlinedTextField(
                value = btMac,
                onValueChange = { btMac = it },
                label = { Text("Printer MAC Address") },
                modifier = Modifier.fillMaxWidth()
            )

            StatusRow("Bluetooth Printer", status.bluetoothConnected)

            if (status.bluetoothConnected) {
                Text("Connected to: ${status.btMac}", color = Color.Green)
            }
        }
    }
}

@Composable
fun StatusRow(label: String, connected: Boolean) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    if (connected) Color.Green else Color.Red,
                    shape = CircleShape
                )
        )

        Spacer(Modifier.width(12.dp))

        Text(
            "$label : ${if (connected) "ONLINE" else "OFFLINE"}",
            fontSize = 18.sp
        )
    }
}