package com.gameonanil.qrattendenceproject.ui.teacher

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*


class SearchAttendanceFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentSearchAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var dateText: String
    private lateinit var semText: String

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
        semText = SearchAttendanceFragmentArgs.fromBundle(requireArguments()).semText

        binding.apply {
            buttonSearchAttendance.setOnClickListener {
                if(etEnterDate.text!!.isNotBlank()){
                    val action =
                        SearchAttendanceFragmentDirections.actionSearchAttendanceFragmentToNewAttendance(dateText,semText)
                    findNavController().navigate(action)
                }else{
                    Toast.makeText(requireContext(), "Please Enter Date First", Toast.LENGTH_SHORT).show()
                }
            }

            etEnterDate.setOnClickListener {
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

    override fun onStart() {
        super.onStart()
        binding.etEnterDate.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = Calendar.getInstance()
        date.set(Calendar.YEAR,year)
        date.set(Calendar.MONTH,month)
        date.set(Calendar.DAY_OF_MONTH,dayOfMonth)

        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date.time)
        dateText = formattedDate
        binding.etEnterDate.setText(dateText)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemLogout) {

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }


}