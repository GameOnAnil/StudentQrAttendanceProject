package com.gameonanil.qrattendenceproject.ui.teacher

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentSearchAttendanceBinding
import java.text.SimpleDateFormat
import java.util.*


class SearchAttendanceFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentSearchAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var dateText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAttendanceBinding.inflate(layoutInflater, container, false)

        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarSearchAttendance,
            navHostFragment,
            appBarConfiguration
        )

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarSearchAttendance)
        binding.toolbarSearchAttendance.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        dateText = ""

        binding.apply {
            buttonSearchAttendance.setOnClickListener {
                if(etEnterDate.text!!.isNotBlank()){
                    val action =
                        SearchAttendanceFragmentDirections.actionSearchAttendanceFragmentToNewAttendance(dateText.toString())
                    findNavController().navigate(action)
                }else{
                    Toast.makeText(requireContext(), "Please Enter Date First", Toast.LENGTH_SHORT).show()
                }
            }

            buttonPickDate.setOnClickListener {
                pickDate()
            }
        }

        return binding.root
    }

    private fun pickDate() {
        val cal: Calendar = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(),this,year,month,day).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (month <10){
            dateText = "$year.0${month+1}.$dayOfMonth"
            binding.etEnterDate.setText(dateText)
        }else{
            dateText = "$year.${month+1}.$dayOfMonth"
            binding.etEnterDate.setText(dateText)

        }

    }

}