package org.mateoUR.apprecordatorios

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// ============================================================
// DataManager
// Gestiona la lectura y escritura de recordatorios y citas
// en archivos JSON en el almacenamiento privado de la app.
// Equivalente al DataMixin de Python.
// ============================================================

data class Medication(
    val id: Int,
    val medName: String,
    val quantity: Int,
    val intervalHours: Int,
    val startTime: String,
    val days: Int,
    val dosesPerDay: Int,
    val medsPerDose: Int
)

data class Appointment(
    val id: Int,
    val name: String,
    val time: String,
    val day: Int,
    val month: Int,
    val year: Int
)

object DataManager {

    private const val MEDICATIONS_FILE  = "medications_data.json"
    private const val APPOINTMENTS_FILE = "appointments_data.json"

    // ── Medicamentos ──────────────────────────────────────────

    fun loadMedications(context: Context): MutableList<Medication> {
        val list = mutableListOf<Medication>()
        try {
            val file = File(context.filesDir, MEDICATIONS_FILE)
            if (!file.exists()) return list
            val array = JSONArray(file.readText(Charsets.UTF_8))
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                list.add(
                    Medication(
                        id           = o.optInt("id", i),
                        medName      = o.getString("med_name"),
                        quantity     = o.getInt("quantity"),
                        intervalHours= o.getInt("interval_hours"),
                        startTime    = o.getString("start_time"),
                        days         = o.getInt("days"),
                        dosesPerDay  = o.getInt("doses_per_day"),
                        medsPerDose  = o.getInt("meds_per_dose")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveMedications(context: Context, medications: List<Medication>) {
        try {
            val array = JSONArray()
            for (m in medications) {
                val o = JSONObject()
                o.put("id",             m.id)
                o.put("med_name",       m.medName)
                o.put("quantity",       m.quantity)
                o.put("interval_hours", m.intervalHours)
                o.put("start_time",     m.startTime)
                o.put("days",           m.days)
                o.put("doses_per_day",  m.dosesPerDay)
                o.put("meds_per_dose",  m.medsPerDose)
                array.put(o)
            }
            saveAtomic(context, MEDICATIONS_FILE, array.toString(4))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Citas ─────────────────────────────────────────────────

    fun loadAppointments(context: Context): MutableList<Appointment> {
        val list = mutableListOf<Appointment>()
        try {
            val file = File(context.filesDir, APPOINTMENTS_FILE)
            if (!file.exists()) return list
            val array = JSONArray(file.readText(Charsets.UTF_8))
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                list.add(
                    Appointment(
                        id    = o.optInt("id", i),
                        name  = o.getString("name"),
                        time  = o.getString("time"),
                        day   = o.getInt("day"),
                        month = o.getInt("month"),
                        year  = o.getInt("year")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveAppointments(context: Context, appointments: List<Appointment>) {
        try {
            val array = JSONArray()
            for (a in appointments) {
                val o = JSONObject()
                o.put("id",    a.id)
                o.put("name",  a.name)
                o.put("time",  a.time)
                o.put("day",   a.day)
                o.put("month", a.month)
                o.put("year",  a.year)
                array.put(o)
            }
            saveAtomic(context, APPOINTMENTS_FILE, array.toString(4))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Guardado seguro con archivo temporal ──────────────────
    private fun saveAtomic(context: Context, filename: String, content: String) {
        val tmp  = File(context.filesDir, "$filename.tmp")
        val dest = File(context.filesDir, filename)
        tmp.writeText(content, Charsets.UTF_8)
        tmp.renameTo(dest)
    }
}
