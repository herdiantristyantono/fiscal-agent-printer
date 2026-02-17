package com.android.fiscalagenttixtax.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

class PermissionManager(
    private val context: Context
) {

    fun requiredBluetoothPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    fun hasPermissions(): Boolean {
        return requiredBluetoothPermissions().all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun rememberBluetoothPermissionRequester(
    onGranted: () -> Unit
): () -> Unit {

    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.values.all { it }

            if (granted) {
                onGranted()
            }
        }

    return {
        if (permissionManager.hasPermissions()) {
            onGranted()
        } else {
            launcher.launch(
                permissionManager.requiredBluetoothPermissions()
            )
        }
    }
}