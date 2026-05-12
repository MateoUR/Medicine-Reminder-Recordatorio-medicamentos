package org.mateoUR.apprecordatorios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// ============================================================
// AlarmReceiver
// Recibe las alarmas exactas disparadas por AlarmManager y
// muestra la notificacion push correspondiente.
// Se ejecuta aunque la app este cerrada o el telefono bloqueado.
// ============================================================

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title   = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE)   ?: "Recordatorio"
        val message = intent.getStringExtra(AlarmScheduler.EXTRA_MESSAGE) ?: ""

        // Limpiar citas pasadas al recibir cualquier alarma
        cleanPastAppointments(context)

        // Asegurar que el canal exista antes de enviar
        NotificationHelper.createChannel(context)
        NotificationHelper.send(context, title, message)
    }

    private fun cleanPastAppointments(context: Context) {
        val now = System.currentTimeMillis()
        val list = DataManager.loadAppointments(context)
        val originalSize = list.size

        list.removeAll { apt ->
            val aptCal = java.util.Calendar.getInstance().apply {
                set(apt.year, apt.month - 1, apt.day)
                val timeParts = apt.time.split(":")
                if (timeParts.size == 2) {
                    set(java.util.Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(java.util.Calendar.MINUTE, timeParts[1].toInt())
                }
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            // Borrar si la cita pasó hace más de 2 horas
            aptCal.timeInMillis + 2 * 3_600_000L < now
        }

        if (list.size != originalSize) {
            DataManager.saveAppointments(context, list)
        }
    }
}
