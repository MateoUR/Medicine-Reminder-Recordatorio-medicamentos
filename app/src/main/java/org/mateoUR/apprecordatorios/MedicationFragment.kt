package org.mateoUR.apprecordatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.mateoUR.apprecordatorios.databinding.FragmentMedicationBinding
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// MedicationFragment
// Pantalla para crear recordatorios de medicamentos.
// Equivalente a MedicationReminderScreen de Python.
// ============================================================

class MedicationFragment : Fragment() {

    private var _binding: FragmentMedicationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            SoundManager.playButton(requireContext())
            findNavController().navigateUp()
        }

        binding.btnDelete.setOnClickListener {
            SoundManager.playButton(requireContext())
            findNavController().navigate(R.id.action_medications_to_delete_medications)
        }

        binding.btnSet.setOnClickListener {
            SoundManager.playButton(requireContext())
            setReminder()
        }
    }

    private fun setReminder() {
        val medName = binding.inputMedName.text.toString().trim()
        val quantityStr = binding.inputQuantity.text.toString().trim()
        val intervalStr = binding.inputInterval.text.toString().trim()
        val startTimeStr = binding.inputStartTime.text.toString().trim()
        val daysStr = binding.inputDays.text.toString().trim()
        val isChronic = binding.checkboxChronic.isChecked

        // Validaciones
        if (medName.isEmpty() || quantityStr.isEmpty() || intervalStr.isEmpty() || startTimeStr.isEmpty()) {
            showStatus("Por favor, complete todos los campos.")
            return
        }

        val quantity = quantityStr.toIntOrNull()
        val intervalHours = intervalStr.toIntOrNull()
        val days = if (isChronic) 365 else daysStr.toIntOrNull()

        if (quantity == null || quantity <= 0 ||
            intervalHours == null || intervalHours <= 0 ||
            days == null || days <= 0) {
            showStatus("Por favor, ingrese valores validos.")
            return
        }

        // Parsear hora inicial
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeParsed = try {
            sdf.parse(startTimeStr)
        } catch (e: Exception) {
            showStatus("Formato de hora invalido. Use HH:MM (Ej: 14:30)")
            return
        } ?: return

        val dosesPerDay = maxOf(1, 24 / intervalHours)
        val totalDoses  = days * dosesPerDay
        val medsPerDose = maxOf(1, quantity / totalDoses)

        // Calcular primera dosis
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startTimeParsed.hours)
            set(Calendar.MINUTE,      startTimeParsed.minutes)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
        }
        if (startCal.before(now)) {
            startCal.add(Calendar.HOUR_OF_DAY, intervalHours)
        }

        // Guardar en JSON
        val medication = Medication(
            id            = Random().nextInt(999999),
            medName       = medName,
            quantity      = quantity,
            intervalHours = intervalHours,
            startTime     = startTimeStr,
            days          = days,
            dosesPerDay   = dosesPerDay,
            medsPerDose   = medsPerDose
        )
        val list = DataManager.loadMedications(requireContext())
        list.add(medication)
        DataManager.saveMedications(requireContext(), list)

        // Programar alarmas exactas para cada dosis
        scheduleAllDoses(medName, medsPerDose, intervalHours, days, dosesPerDay, startCal)

        clearInputs()
        showStatus("Recordatorios establecidos para $medName.")
    }

    private fun scheduleAllDoses(
        medName: String,
        medsPerDose: Int,
        intervalHours: Int,
        days: Int,
        dosesPerDay: Int,
        startCal: Calendar
    ) {
        val cal = startCal.clone() as Calendar
        val ctx = requireContext()

        repeat(days) {
            repeat(dosesPerDay) {
                val title   = "Es hora de tomar tu medicamento"
                val timeStr = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                val message = "$medName — $medsPerDose unidad(es) ($timeStr)"
                AlarmScheduler.schedule(ctx, cal.timeInMillis, title, message)
                cal.add(Calendar.HOUR_OF_DAY, intervalHours)
            }
        }
    }

    private fun clearInputs() {
        binding.inputMedName.text.clear()
        binding.inputQuantity.text.clear()
        binding.inputInterval.text.clear()
        binding.inputStartTime.text.clear()
        binding.inputDays.text.clear()
        binding.checkboxChronic.isChecked = false
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
