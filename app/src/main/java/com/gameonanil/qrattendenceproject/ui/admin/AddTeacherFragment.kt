package com.gameonanil.qrattendenceproject.ui.admin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentAddTeacherBinding
import com.gameonanil.qrattendenceproject.model.Teacher
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.gameonanil.qrattendenceproject.ui.teacher.MainTeacherFragment
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
    private lateinit var spinnerArray: ArrayList<String>
    private lateinit var adminEmail: String
    private lateinit var adminPass:String

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

        adminEmail = AddTeacherFragmentArgs.fromBundle(requireArguments()).adminEmail
        adminPass = AddTeacherFragmentArgs.fromBundle(requireArguments()).adminPassword
        Log.d(TAG, "onCreateView: TEACHR:EMAIL=$adminEmail and password=$adminPass")

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        spinnerArray = ArrayList()
        spinnerArray.add("")

        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.subject_drop_down, spinnerArray)
        binding.spinner.adapter = spinnerAdapter

        binding.apply {

            buttonAddTeacher.setOnClickListener {
                if (etUserName.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please Enter User Name",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
                if (etEmail.text.toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please Enter Email", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                if (etPass.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please Enter Password",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
                nameString = etUserName.text!!.trim().toString()
                if (etPhone.text!!.isNotEmpty()) {
                    phoneString = etPhone.text.toString()
                }
                if (etAddress.text!!.isNotEmpty()) {
                    addressString = etAddress.text.toString()
                }

                if(spinnerArray.isEmpty()||spinnerArray.size==1){
                    Toast.makeText(
                        requireContext(),
                        "Please Add Atleast 1 Subject",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }


                val email: String = etEmail.text!!.trim().toString()
                val password: String = etPass.text!!.trim().toString()
                if (email.isNotEmpty() || password.isNotEmpty()) {
                    signUpUser(email, password)
                }
            }

            buttonAddSub.setOnClickListener {
                handleAddSubjectClicked()
                spinnerAdapter.notifyDataSetChanged()
            }

            buttonRemove.setOnClickListener {
                val currentText = binding.spinner.selectedItem.toString()
                if (currentText!=""){
                    spinnerArray.remove(currentText)
                    spinnerAdapter.notifyDataSetChanged()
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

    private fun handleAddSubjectClicked(){
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_text,null)
        val editText = dialogLayout.findViewById<EditText>(R.id.etSubjectDialog)

        with(builder){
            setTitle("Enter Subject Name")
            setPositiveButton("Confirm"){dialog,which->
                if (editText.text.toString().isNotEmpty()){
                    spinnerArray.add(editText.text.toString())
                }else{
                    Toast.makeText(requireContext(), "Subject Empty!", Toast.LENGTH_SHORT).show()
                }

            }
            setNegativeButton("Cancel"){dialog,which->
                Log.d(TAG, "handleAddSubjectClicked: cancel clicked")
            }
            setView(dialogLayout)
            show()
        }

    }

    private fun signUpUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newTeacherUser: FirebaseUser = task.result!!.user!!

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
        val finalSubArray = spinnerArray
        finalSubArray.removeAt(0)
        val collectionReference = firestore.collection("users")
        val userModel = Teacher(
            uid = user.uid,
            email = user.email,
            address = addressString,
            phone = phoneString,
            user_type = "teacher",
            username = nameString,
            subject = finalSubArray
        )

        collectionReference.document(user.uid).set(userModel)
            .addOnSuccessListener {

                Toast.makeText(
                    requireContext(),
                    "SignUp Successful",
                    Toast.LENGTH_SHORT
                )
                    .show()
                mAuth.signOut()
                loginToAdmin()

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
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