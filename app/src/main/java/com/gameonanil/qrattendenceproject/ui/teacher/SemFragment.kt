package com.gameonanil.qrattendenceproject.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentSemBinding
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth


class SemFragment : Fragment(){
    companion object{
        private const val TAG = "SemFragment"
    }

    private var _binding: FragmentSemBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var semText: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.semFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarSem,
            navHostFragment,
            appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarSem)
        binding.toolbarSem.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        mAuth = FirebaseAuth.getInstance()


        binding.apply {
            autoCompleteSemester.inputType = EditorInfo.TYPE_NULL
            buttonSemGo.setOnClickListener {
               goToNextPage()
            }
        }


        return binding.root
    }

    private fun goToNextPage(){
        semText = binding.autoCompleteSemester.text.toString()
        val action = SemFragmentDirections.actionSemFragmentToMainTeacherFragment(semText)
        findNavController().navigate(action)
    }

    /**SETTING UP DROPDOWN MENU **/
    override fun onResume() {
        super.onResume()
        val semTypes = resources.getStringArray(R.array.sem_temp)
        val arrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item,semTypes)
        binding.autoCompleteSemester.setAdapter(arrayAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId ==R.id.itemLogout){
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }

}