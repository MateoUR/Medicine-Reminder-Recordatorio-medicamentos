package org.mateoUR.apprecordatorios

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlin.random.Random

// ============================================================
// AlarmScheduler
// Programa alarmas exactas con AlarmManager usando
// setExactAndAllowWhileIdle, que despierta el dispositivo
// incluso en modo Doze (ahorro de batería).
// Equivalente a schedule_alarm() de Python.
// ============================================================

object AlarmScheduler {

    const val EXTRA_TITLE   = "notif_title"
    const val EXTRA_MESSAGE = "notif_message"
    const val EXTRA_ALARM_ID = "alarm_id"

    fun schedule(
        context: Context,
        triggerEpochMs: Long,
        title: String,
        message: String
    ) {
        val alarmId = Random.nextInt(1, 999999)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE,    title)
            putExtra(EXTRA_MESSAGE,  message)
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE
                else 0

        val pending = PendingIntent.getBroadcast(context, alarmId, intent, flags)

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        // setExactAndAllowWhileIdle: despierta aunque el dispositivo este en Doze
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerEpochMs,
            pending
        )
    }

    // Cancela una alarma previamente programada por su ID
    fun cancel(context: Context, alarmId: Int) {
        val intent  = Intent(context, AlarmReceiver::class.java)
        val flags   = PendingIntent.FLAG_NO_CREATE or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE
                else 0
        val pending = PendingIntent.getBroadcast(context, alarmId, intent, flags)
        pending?.let {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.cancel(it)
        }
    }
}
