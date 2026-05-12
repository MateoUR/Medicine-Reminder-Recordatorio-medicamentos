package org.mateoUR.apprecordatorios

import android.app.Service
import android.content.Intent
import android.os.IBinder

// ============================================================
// ReminderService
// Foreground Service que mantiene la app viva en segundo plano.
// Muestra una notificacion persistente (requerido por Android
// para servicios en primer plano) y reprograma alarmas al
// iniciarse.
// ============================================================

class ReminderService : Service() {

    companion object {
        const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Notificacion persistente obligatoria para Foreground Service
        val notification = NotificationHelper.buildNotification(
            this,
            "Recordatorios activos",
            "Vigilando tus recordatorios de salud..."
        )
        startForeground(NOTIF_ID, notification)

        // El servicio se reinicia automaticamente si Android lo mata
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
