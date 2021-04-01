package com.gameonanil.qrattendenceproject.ui.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentMainTeacherBinding


class MainTeacherFragment : Fragment() {
    private var _binding: FragmentMainTeacherBinding? = null
    private val binding get()= _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainTeacherBinding.inflate(inflater, container, false)



        binding.apply {
            fabTeacher.setOnClickListener {
                val action = MainTeacherFragmentDirections.actionMainTeacherFragmentToGeneratorFragment()
                findNavController().navigate(action)
            }
        }





        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}