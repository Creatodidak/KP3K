package id.creatodidak.kp3k.dashboard.ui.laporansaya.prog2a

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentListLahanProg2aBinding

class ListLahanProg2aFragment : Fragment() {
    private var _binding: FragmentListLahanProg2aBinding? = null;
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListLahanProg2aBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.pilihLahan.setOnClickListener {
            val action = ListLahanProg2aFragmentDirections
                .actionListLahanProg2aFragmentToListProgram2aFragment("Lahan A", 0)
            findNavController().navigate(action)
        }

        return root
    }
}