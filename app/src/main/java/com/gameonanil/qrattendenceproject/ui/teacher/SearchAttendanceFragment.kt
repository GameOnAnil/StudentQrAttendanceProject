package com.gameonanil.qrattendenceproject.ui.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R

import com.gameonanil.qrattendenceproject.databinding.FragmentSearchAttendanceBinding


class SearchAttendanceFragment : Fragment() {

    private var _binding: FragmentSearchAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAttendanceBinding.inflate(layoutInflater,container,false)

        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(binding.toolbarSearchAttendance, navHostFragment,appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarSearchAttendance)
        binding.toolbarSearchAttendance.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }


        binding.apply {
            buttonSearchAttendance.setOnClickListener {
                val action = SearchAttendanceFragmentDirections.actionSearchAttendanceFragmentToNewAttendance()
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