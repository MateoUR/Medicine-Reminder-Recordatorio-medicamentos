package org.mateoUR.apprecordatorios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

// ============================================================
// BootReceiver
// Se ejecuta al encender el dispositivo.
// Android cancela todas las alarmas al apagarse, asi que este
// receiver las reprograma leyendo los JSON guardados.
// Equivalente a reprogramar_alarmas() de service.py.
// ============================================================

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON") return

        NotificationHelper.createChannel(context)

        val now = System.currentTimeMillis()

        // ── Reprogramar medicamentos ───────────────────────────
        val medications = DataManager.loadMedications(context)
        for (med in medications) {
            val parts = med.startTime.split(":")
            if (parts.size != 2) continue
            val startH = parts[0].toIntOrNull() ?: continue
            val startM = parts[1].toIntOrNull() ?: continue

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startH)
                set(Calendar.MINUTE,      startM)
                set(Calendar.SECOND,      0)
                set(Calendar.MILLISECOND, 0)
            }

            // Avanzar a la primera dosis futura
            while (cal.timeInMillis <= now) {
                cal.add(Calendar.HOUR_OF_DAY, med.intervalHours)
            }

            val endMs = now + med.days.toLong() * 24 * 60 * 60 * 1000

            while (cal.timeInMillis < endMs) {
                val title   = "Es hora de tomar tu medicamento"
                val message = "${med.medName} — ${med.medsPerDose} unidad(es) (${med.startTime})"
                AlarmScheduler.schedule(context, cal.timeInMillis, title, message)
                cal.add(Calendar.HOUR_OF_DAY, med.intervalHours)
            }
        }

        // ── Reprogramar citas ──────────────────────────────────
        val appointments = DataManager.loadAppointments(context)
        for (apt in appointments) {
            val parts = apt.time.split(":")
            if (parts.size != 2) continue
            val h = parts[0].toIntOrNull() ?: continue
            val m = parts[1].toIntOrNull() ?: continue

            val aptCal = Calendar.getInstance().apply {
                set(Calendar.YEAR,         apt.year)
                set(Calendar.MONTH,        apt.month - 1)
                set(Calendar.DAY_OF_MONTH, apt.day)
                set(Calendar.HOUR_OF_DAY,  h)
                set(Calendar.MINUTE,       m)
                set(Calendar.SECOND,       0)
                set(Calendar.MILLISECOND,  0)
            }
            val aptMs = aptCal.timeInMillis

            val alerts = listOf(
                aptMs - 86_400_000L to "Tu cita ${apt.name} es manana",
                aptMs -  3_600_000L to "Tu cita ${apt.name} es en 1 hora",
                aptMs               to "Es la hora de tu cita: ${apt.name}"
            )
            for ((triggerMs, msg) in alerts) {
                if (triggerMs > now) {
                    AlarmScheduler.schedule(
                        context, triggerMs,
                        "Recordatorio de Cita Medica", msg
                    )
                }
            }
        }
    }
}
