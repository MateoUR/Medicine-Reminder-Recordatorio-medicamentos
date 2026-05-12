package org.mateoUR.apprecordatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.mateoUR.apprecordatorios.databinding.FragmentDeleteAppointmentBinding

// ============================================================
// DeleteAppointmentFragment
// Pantalla para eliminar una cita medica por indice.
// Equivalente a DeleteAppointmentsScreen de Python.
// ============================================================

class DeleteAppointmentFragment : Fragment() {

    private var _binding: FragmentDeleteAppointmentBinding? = null
    private val binding get() = _binding!!
    private var appointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSpinner()

        binding.btnDelete.setOnClickListener {
            SoundManager.playButton(requireContext())
            deleteSelected()
        }
        binding.btnBack.setOnClickListener {
            SoundManager.playButton(requireContext())
            findNavController().navigateUp()
        }
    }

    private fun loadSpinner() {
        appointments = DataManager.loadAppointments(requireContext())
        val labels = appointments.mapIndexed { i, a ->
            "[$i] ${a.name} — ${String.format("%02d/%02d/%d %s", a.day, a.month, a.year, a.time)}"
        }
        binding.spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            labels
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun deleteSelected() {
        val idx = binding.spinner.selectedItemPosition
        if (idx < 0 || idx >= appointments.size) return

        appointments.removeAt(idx)
        DataManager.saveAppointments(requireContext(), appointments)
        loadSpinner()
        binding.labelStatus.text = "Cita eliminada."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
