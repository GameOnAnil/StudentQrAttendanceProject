package com.gameonanil.qrattendenceproject.ui.admin

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentAddTeacherBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentManageUserBinding
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth


class ManageUserFragment : Fragment() {
    private var _binding: FragmentManageUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       _binding = FragmentManageUserBinding.inflate(inflater,container,false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarManageUser,
            navHostFragment,
            appBarConfiguration
        )

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarManageUser)
        binding.toolbarManageUser.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }
        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_logout, menu)
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