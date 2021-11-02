package com.gameonanil.qrattendenceproject.ui.admin

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.ActivityEditStudentBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentEditStudentBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentUserDetailBinding
import com.gameonanil.qrattendenceproject.model.Student
import com.gameonanil.qrattendenceproject.model.Users
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.gameonanil.qrattendenceproject.ui.student.EditStudentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EditStudentFragment : Fragment() {
    companion object{
        private const val TAG = "EditStudentFragment"
    }

    private var _binding: FragmentEditStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var studentUid: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditStudentBinding.inflate(layoutInflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarEdit,
            navHostFragment,
            appBarConfiguration
        )
        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarEdit)
        binding.toolbarEdit.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        studentUid = EditStudentFragmentArgs.fromBundle(requireArguments()).studentUid
        Log.d(TAG, "onCreate:studentuID++++++$studentUid ")
        initDetails(studentUid)

       binding.apply {
            buttonAddTeacher.setOnClickListener {
                if (etUserName.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please Enter User Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (etRoll.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please Enter Roll",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                saveDetail(studentUid)
            }
        }


        return binding.root
    }

    private fun saveDetail(studentUid: String) {
        val studentHashMap = HashMap<String, Any>()
        studentHashMap["username"] = binding.etUserName.text.toString()
        studentHashMap["roll"] = binding.etRoll.text.toString().toInt()

        studentHashMap["address"] = binding.etAddress.text!!.toString()
        studentHashMap["phone"] = binding.etPhone.text!!.toString()


        val collectionRef = firestore.collection("users")
        collectionRef.document(studentUid).update(studentHashMap)
            .addOnSuccessListener {

                //update attendance
                val attendanceRef = firestore
                    .collection("attendance")
                attendanceRef.get().addOnSuccessListener { QuerySnapshot ->
                    for (querySnapshot in QuerySnapshot) {
                        val currentTeacherUid = querySnapshot.id.toString()
                        modifyAttendance(currentTeacherUid, studentHashMap)
                    }
                }.addOnFailureListener {
                    Log.d(TAG, "saveDetail: FAILED AT modiflying attendance")
                }
                Toast.makeText(requireContext(), "Saved Successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error:${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun modifyAttendance(currentTeacherUid: String, userHashMap: HashMap<String, Any>) {
        val collectionRef = firestore.collection("attendance")
        collectionRef.document(currentTeacherUid).collection("student_list").get()
            .addOnSuccessListener { QuerySnapshot ->
                for (query in QuerySnapshot) {
                    if (query.reference.id == studentUid) {
                        query.reference.update(userHashMap)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "ERROR:${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun goBack(){
        findNavController().navigateUp()
    }

    private fun initDetails(studentUid: String) {
        val collectionRef = firestore.collection("users")
        collectionRef.document(studentUid).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val currentUser = documentSnapshot.toObject(Student::class.java)
                if (currentUser != null) {
                    binding.etUserName.setText(currentUser.username)
                    binding.etAddress.setText(currentUser.address)
                    binding.etPhone.setText(currentUser.phone)
                    binding.etRoll.setText(currentUser.roll.toString())
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "ERROR:${it.message}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "initDetails: ERROR_${it.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_logout, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemLogout) {
            Log.d(ContentValues.TAG, "onOptionsItemSelected: logout pressed")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }
}