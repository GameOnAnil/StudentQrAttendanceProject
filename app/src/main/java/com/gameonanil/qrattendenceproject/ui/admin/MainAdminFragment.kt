package com.gameonanil.qrattendenceproject.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentMainAdminBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentMainTeacherBinding
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.gameonanil.qrattendenceproject.ui.teacher.MainTeacherFragment
import com.gameonanil.qrattendenceproject.ui.teacher.MainTeacherFragmentDirections
import com.google.firebase.auth.FirebaseAuth


class MainAdminFragment : Fragment() {
    companion object{
        private const val TAG = "MainAdminFragment"
    }

    private var _binding: FragmentMainAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var  appBarConfiguration:AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainAdminBinding.inflate(layoutInflater,container,false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarAdminMain,
            navHostFragment,
            appBarConfiguration
        )


        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarAdminMain)
        binding.toolbarAdminMain.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }



        binding.apply {
            buttonAddStudent.setOnClickListener {
               val action = MainAdminFragmentDirections.actionMainAdminFragmentToAddStudentFragment()
                findNavController().navigate(action)
            }

            buttonAddTeacher.setOnClickListener {
                val action = MainAdminFragmentDirections.actionMainAdminFragmentToAddTeacherFragment()
                findNavController().navigate(action)
            }
        }



        return binding.root
    }

    //handling options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemLogout) {
            Log.d(TAG, "onOptionsItemSelected: logout pressed")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return super.onOptionsItemSelected(item)
    }


}