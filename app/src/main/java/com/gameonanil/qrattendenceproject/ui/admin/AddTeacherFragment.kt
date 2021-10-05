package com.gameonanil.qrattendenceproject.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentAddTeacherBinding
import com.gameonanil.qrattendenceproject.model.Teacher
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class AddTeacherFragment : Fragment() {
    companion object {
        private const val TAG = "`AddTeacherFragment`"
    }

    private var _binding: FragmentAddTeacherBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var nameString: String = ""
    private var phoneString: String = ""
    private var addressString: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTeacherBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarAddTeacher,
            navHostFragment,
            appBarConfiguration
        )

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarAddTeacher)
        binding.toolbarAddTeacher.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        binding.apply {

            buttonAddTeacher.setOnClickListener {
                if (etUserName.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please Enter User Name", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                if (etEmail.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please Enter Email", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                if (etPass.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please Enter Password", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                nameString = etUserName.text.toString()
                if (etPhone.text.isNotEmpty()) {
                    phoneString = etPhone.text.toString()
                }
                if (etAddress.text.isNotEmpty()) {
                    addressString = etAddress.text.toString()
                }


                val email: String = etEmail.text.toString()
                val password: String = etPass.text.toString()
                if (email.isNotEmpty() || password.isNotEmpty()) {
                    signUpUser(email, password)
                }
            }

        }


        return binding.root
    }

    private fun signUpUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newTeacherUser: FirebaseUser = task.result!!.user!!

                    Toast.makeText(requireContext(), "User SignUp Successful", Toast.LENGTH_SHORT)
                        .show()
                    addDetailsToDb(newTeacherUser)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Sign Up Failed:${task.exception!!.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "signUpUser: SignUP Failed!!!!!!!")
                }
            }
    }

    private fun addDetailsToDb(user: FirebaseUser) {
        val collectionReference = firestore.collection("users")
        val userModel = Teacher(
            uid = user.uid,
            email = user.email,
            address = addressString,
            phone = phoneString,
            user_type = "teacher",
            username = nameString
        )

        collectionReference.document(user.uid).set(userModel)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Detail Added Successfully", Toast.LENGTH_SHORT)
                    .show()
                mAuth.signOut()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "addDetailsToDb: ERROR: ${it.message}")
            }
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