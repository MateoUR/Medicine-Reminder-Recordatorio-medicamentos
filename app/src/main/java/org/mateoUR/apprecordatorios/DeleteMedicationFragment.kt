package org.mateoUR.apprecordatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.mateoUR.apprecordatorios.databinding.FragmentDeleteMedicationBinding

// ============================================================
// DeleteMedicationFragment
// Pantalla para eliminar un recordatorio de medicamento
// por indice, evitando el bug de borrado masivo por nombre.
// Equivalente a DeleteRemindersScreen de Python.
// ============================================================

class DeleteMedicationFragment : Fragment() {

    private var _binding: FragmentDeleteMedicationBinding? = null
    private val binding get() = _binding!!
    private var medications = mutableListOf<Medication>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteMedicationBinding.inflate(inflater, container, false)
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
        medications = DataManager.loadMedications(requireContext())
        val labels = medications.mapIndexed { i, m -> "[$i] ${m.medName}" }
        binding.spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            labels
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun deleteSelected() {
        val idx = binding.spinner.selectedItemPosition
        if (idx < 0 || idx >= medications.size) return

        medications.removeAt(idx)
        DataManager.saveMedications(requireContext(), medications)
        loadSpinner()
        binding.labelStatus.text = "Recordatorio eliminado."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
