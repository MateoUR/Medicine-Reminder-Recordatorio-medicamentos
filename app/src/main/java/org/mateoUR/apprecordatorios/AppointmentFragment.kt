package org.mateoUR.apprecordatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.mateoUR.apprecordatorios.databinding.FragmentAppointmentBinding
import java.util.*

// ============================================================
// AppointmentFragment
// Pantalla para crear citas medicas con seleccion de hora
// y fecha, y alertas anticipadas (1 dia y 1 hora antes).
// Equivalente a MedicalAppointmentsScreen de Python.
// ============================================================

class AppointmentFragment : Fragment() {

    private var _binding: FragmentAppointmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cleanPastAppointments()
        setupSpinners()

        binding.btnBack.setOnClickListener {
            SoundManager.playButton(requireContext())
            findNavController().navigateUp()
        }

        binding.btnDelete.setOnClickListener {
            SoundManager.playButton(requireContext())
            findNavController().navigate(R.id.action_appointments_to_delete_appointments)
        }

        binding.btnSet.setOnClickListener {
            SoundManager.playButton(requireContext())
            setAppointment()
        }
    }

    private fun setupSpinners() {
        val ctx = requireContext()

        // Horas 00-23
        binding.spinnerHour.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_item,
            (0..23).map { String.format("%02d", it) }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Minutos 00,05,10,...,55
        binding.spinnerMinute.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_item,
            (0..59 step 5).map { String.format("%02d", it) }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Dias 1-31
        binding.spinnerDay.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_item,
            (1..31).map { it.toString() }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Meses 1-12
        binding.spinnerMonth.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_item,
            (1..12).map { it.toString() }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Anos: actual hasta actual+6
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        binding.spinnerYear.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_item,
            (currentYear..currentYear + 6).map { it.toString() }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun cleanPastAppointments() {
        val context = context ?: return
        val now = System.currentTimeMillis()
        val list = DataManager.loadAppointments(context)
        val originalSize = list.size

        list.removeAll { apt ->
            val aptCal = Calendar.getInstance().apply {
                set(apt.year, apt.month - 1, apt.day)
                val timeParts = apt.time.split(":")
                if (timeParts.size == 2) {
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(Calendar.MINUTE, timeParts[1].toInt())
                }
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Borrar si la cita pasó hace más de 2 horas
            aptCal.timeInMillis + 2 * 3_600_000L < now
        }

        if (list.size != originalSize) {
            DataManager.saveAppointments(context, list)
        }
    }

    private fun setAppointment() {
        val name = binding.inputName.text.toString().trim()
        if (name.isEmpty()) {
            showStatus("Ingrese un nombre para la cita.")
            return
        }

        val hour   = binding.spinnerHour.selectedItem.toString().toInt()
        val minute = binding.spinnerMinute.selectedItem.toString().toInt()
        val day    = binding.spinnerDay.selectedItem.toString().toInt()
        val month  = binding.spinnerMonth.selectedItem.toString().toInt()
        val year   = binding.spinnerYear.selectedItem.toString().toInt()

        // Validar fecha
        val aptCal = try {
            Calendar.getInstance().apply {
                isLenient = false
                set(year, month - 1, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }.also { it.time } // fuerza la validacion
            Calendar.getInstance().apply {
                set(year, month - 1, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            showStatus("Fecha invalida (ese mes no tiene ese dia).")
            return
        }

        val timeStr = String.format("%02d:%02d", hour, minute)

        // Guardar cita
        val appointment = Appointment(
            id    = Random().nextInt(999999),
            name  = name,
            time  = timeStr,
            day   = day,
            month = month,
            year  = year
        )
        val list = DataManager.loadAppointments(requireContext())
        list.add(appointment)
        DataManager.saveAppointments(requireContext(), list)

        // Programar las 3 alertas
        scheduleAppointmentAlerts(name, aptCal)

        binding.inputName.text.clear()
        showStatus("Cita '$name' — ${String.format("%02d/%02d/%d %s", day, month, year, timeStr)}.")
    }

    private fun scheduleAppointmentAlerts(name: String, aptCal: Calendar) {
        val aptMs = aptCal.timeInMillis
        val now   = System.currentTimeMillis()
        val ctx   = requireContext()

        val alerts = listOf(
            aptMs - 7 * 86_400_000L to "Tu cita $name es en 1 semana",
            aptMs - 3 * 86_400_000L to "Tu cita $name es en 3 dias",
            aptMs - 86_400_000L     to "Tu cita $name es manana",
            aptMs - 6 * 3_600_000L  to "Tu cita $name es hoy en 6 horas",
            aptMs                  to "Es la hora de tu cita: $name"
        )
        for ((triggerMs, msg) in alerts) {
            if (triggerMs > now) {
                AlarmScheduler.schedule(ctx, triggerMs, "Recordatorio de Cita Medica", msg)
            }
        }
    }

    private fun showStatus(msg: String) {
        binding.labelStatus.text = msg
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
