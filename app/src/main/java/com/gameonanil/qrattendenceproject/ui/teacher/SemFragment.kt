package com.gameonanil.qrattendenceproject.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.view.*
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


class SemFragment : Fragment() {

    private var _binding: FragmentSemBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth

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
            buttonSemGo.setOnClickListener {
                val action = SemFragmentDirections.actionSemFragmentToMainTeacherFragment2()
                findNavController().navigate(action)

            }
        }


        return binding.root
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