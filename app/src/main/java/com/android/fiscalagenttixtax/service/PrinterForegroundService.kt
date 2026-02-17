package com.android.fiscalagenttixtax.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.android.fiscalagenttixtax.R
import com.android.fiscalagenttixtax.app.App
import kotlinx.coroutines.*

class PrinterForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        startPrinterEngine()
    }

    private fun startForegroundNotification() {

        val channelId = "printer_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Printer Service",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fiscal Printer Running")
            .setContentText("Printer service is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun startPrinterEngine() {
        val app = application as App
        app.startFiscalAgentUsb()
        app.startFiscalAgentLanToBluetooth()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        val app = application as App
        app.shutdownPrinters()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}