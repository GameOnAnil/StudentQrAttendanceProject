package com.gameonanil.qrattendenceproject.ui.teacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentNewAttendanceBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentSearchAttendanceBinding

class NewAttendance : Fragment() {

    private var _binding: FragmentNewAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAttendanceBinding.inflate(layoutInflater,container,false)

        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(binding.toolbarNewAttendance, navHostFragment,appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarNewAttendance)
        binding.toolbarNewAttendance.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        return binding.root
    }

}