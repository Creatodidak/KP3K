package id.creatodidak.kp3k.dashboard.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentGantiFotoBinding


class GantiFotoFragment : Fragment() {
    private lateinit var _binding: FragmentGantiFotoBinding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGantiFotoBinding.inflate(inflater, container, false)
        val root : View = binding.root

        return root
    }
}