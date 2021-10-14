package com.gameonanil.qrattendenceproject.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentAddStudentBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentMainAdminBinding
import com.gameonanil.qrattendenceproject.model.Student
import com.gameonanil.qrattendenceproject.model.Teacher
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.gameonanil.qrattendenceproject.ui.teacher.MainTeacherFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class AddStudentFragment : Fragment() {
    companion object{
        private const val TAG = "AddStudentFragment"
    }

    private var _binding: FragmentAddStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var nameString: String = ""
    private var phoneString: String = ""
    private var addressString: String = ""
    private var rollNumber: Int? = null
    private lateinit var adminEmail: String
    private lateinit var adminPass: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentAddStudentBinding.inflate(inflater,container,false)


        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarAddStudent,
            navHostFragment,
            appBarConfiguration
        )



        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarAddStudent)
        binding.toolbarAddStudent.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        adminEmail = AddStudentFragmentArgs.fromBundle(requireArguments()).adminEmail
        adminPass = AddStudentFragmentArgs.fromBundle(requireArguments()).adminPassword
        Log.d(TAG, "onCreateView: STUDENT :EMAIL=$adminEmail and password=$adminPass")


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
                if (etRoll.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please Enter Roll ", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                progressbarAddStudent.isVisible = true
                buttonAddTeacher.isEnabled = false

                nameString = etUserName.text!!.trim().toString()
                rollNumber = etRoll.text.toString().trim().toInt()
                if (etPhone.text!!.isNotEmpty()) {
                    phoneString = etPhone.text.toString()
                }
                if (etAddress.text!!.isNotEmpty()) {
                    addressString = etAddress.text.toString()
                }

                val email: String = etEmail.text!!.trim().toString()
                val password: String = etPass.text!!.trim().toString()
                if (email.isNotEmpty() || password.isNotEmpty()) {
                    signUpUser(email, password)
                }
            }

        }


        return binding.root
    }

    private fun loginToAdmin(){
        mAuth.signInWithEmailAndPassword(adminEmail,adminPass)
            .addOnSuccessListener {
                Log.d(TAG, "loginToAdmin: SUCCESS")
                findNavController().navigateUp()
            }.addOnFailureListener {
                Log.d(TAG, "loginToAdmin: FAILED:${it.message}")
                findNavController().navigateUp()
            }
    }

    private fun signUpUser(email: String, password: String)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newTeacherUser: FirebaseUser = task.result!!.user!!
                    addDetailsToDb(newTeacherUser,password)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Sign Up Failed:${task.exception!!.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "signUpUser: SignUP Failed!!!!!!!")
                    binding.progressbarAddStudent.isVisible = false
                    binding.buttonAddTeacher.isEnabled = true
                }
            }
    }

    private fun addDetailsToDb(user: FirebaseUser,passwordText: String) {
        val collectionReference = firestore.collection("users")
        val userModel = Student(
            uid = user.uid,
            email = user.email,
            password = passwordText,
            address = addressString,
            phone = phoneString,
            user_type = "student",
            username = nameString,
            roll = rollNumber
        )

        collectionReference.document(user.uid).set(userModel)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User SignUp Successful", Toast.LENGTH_SHORT)
                    .show()
                mAuth.signOut()
                binding.progressbarAddStudent.isVisible = false
                binding.buttonAddTeacher.isEnabled = true
                loginToAdmin()

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "addDetailsToDb: ERROR: ${it.message}")
                binding.progressbarAddStudent.isVisible = false
                binding.buttonAddTeacher.isEnabled = true
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_logout,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemLogout){
            Log.d(TAG, "onOptionsItemSelected: logout pressed")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }

}