package org.mateoUR.apprecordatorios

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.ContentResolver
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlin.random.Random

// ============================================================
// NotificationHelper
// Crea el canal de notificaciones y envía notificaciones push
// que aparecen en pantalla bloqueada y con app cerrada.
// Equivalente a send_notification() de Python.
// ============================================================

object NotificationHelper {

    const val CHANNEL_ID   = "canal_hospital_01"
    const val CHANNEL_NAME = "Recordatorios de Salud"

    // Crea el canal una sola vez (Android 8+, llamar en Application.onCreate)
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel    = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                enableVibration(true)
                enableLights(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                
                val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.sound_notification)
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Construye una notificación lista para enviar (también usada por el Foreground Service)
    fun buildNotification(context: Context, title: String, message: String): Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.sound_notification))
            .setContentIntent(tapPending)
            .build()
    }

    // Envía la notificación inmediatamente
    fun send(context: Context, title: String, message: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(Random.nextInt(1, 999999), buildNotification(context, title, message))
    }
}
