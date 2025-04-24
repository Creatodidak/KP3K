package id.creatodidak.kp3k.dashboard.ui.laporansaya

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentPilihProgramLaporanBinding

class PilihProgramLaporan : Fragment() {

    private var _binding: FragmentPilihProgramLaporanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPilihProgramLaporanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.prog1.setOnClickListener {
            findNavController().navigate(R.id.pilihProgramLaporan)
        }

        binding.prog2a.setOnClickListener {
            findNavController().navigate(R.id.nav_list_lahan_program2a)
        }

        binding.prog2b.setOnClickListener {
            findNavController().navigate(R.id.pilihProgramLaporan)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}