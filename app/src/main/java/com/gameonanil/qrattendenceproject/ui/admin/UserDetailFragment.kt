package com.gameonanil.qrattendenceproject.ui.admin

import android.content.ContentValues
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
import com.gameonanil.qrattendenceproject.databinding.FragmentUserDetailBinding
import com.gameonanil.qrattendenceproject.model.Users
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth


class UserDetailFragment : Fragment() {
    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mCurrentUser: Users

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserDetailBinding.inflate(layoutInflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarUserDetail,
            navHostFragment,
            appBarConfiguration
        )
        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarUserDetail)
        binding.toolbarUserDetail.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        mCurrentUser = UserDetailFragmentArgs.fromBundle(requireArguments()).currentUser


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initDetail()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initDetail(){
        binding.apply {
            tvUserName.text = mCurrentUser.username
            tvEmail.text = mCurrentUser.email
            tvPhone.text = mCurrentUser.phone
            tvAddress.text = mCurrentUser.address
            if (mCurrentUser.user_type == "student") {
                tvUserType.text = "Student"
            }
            if(mCurrentUser.user_type=="teacher"){
                tvUserType.text = "Teacher"
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemLogout) {
            Log.d(ContentValues.TAG, "onOptionsItemSelected: logout pressed")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        if (item.itemId == R.id.item_edit){
            if(mCurrentUser.user_type=="student"){
                val action = UserDetailFragmentDirections.actionUserDetailFragmentToEditStudentFragment(mCurrentUser.uid!!.trim().toString())
                findNavController().navigate(action)
            }
        }

        return super.onOptionsItemSelected(item)
    }

}