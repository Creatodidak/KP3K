package id.creatodidak.kp3k.dashboard.ui.lahantugas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentPilihProgramLaporanBinding
import id.creatodidak.kp3k.databinding.FragmentRincianLahanBinding

class RincianLahanFragment : Fragment() {
    private var _binding: FragmentRincianLahanBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRincianLahanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }
}