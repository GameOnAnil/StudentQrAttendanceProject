package com.gameonanil.qrattendenceproject.ui.teacher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R

import com.gameonanil.qrattendenceproject.databinding.FragmentStudentsDetailBinding
import com.gameonanil.qrattendenceproject.model.Student
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class StudentsDetailFragment : Fragment() {
    companion object{
        private const val TAG = "StudentsDetailFragment"
    }

    private var _binding: FragmentStudentsDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var currentUser : Student
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var subjectText:String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsDetailBinding.inflate(layoutInflater,container,false)

        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(binding.toolbarStudentDetail, navHostFragment,appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarStudentDetail)
        binding.toolbarStudentDetail.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        currentUser = StudentsDetailFragmentArgs.fromBundle(requireArguments()).userDetail
        Log.d(TAG, "onCreateView: USER!!!!!!!!!!!!!!!!!!${currentUser.username}")

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        subjectText = StudentsDetailFragmentArgs.fromBundle(requireArguments()).subjectText


        binding.apply {
            currentUser.username?.let { tvUserName.text = it}
            currentUser.email?.let { tvEmail.text = it}
            currentUser.phone?.let{tvPhone.text = it}
            currentUser.address?.let{tvAddress.text = it}


           displayTotalAttendance()
        }

        return binding.root
    }

    private fun displayTotalAttendance(){

            val studentUid = currentUser.uid
            val docRef = firestore.collection("attendance_count")
                .document(studentUid!!)
                .collection("subject")
                .document(subjectText)
            Log.d(TAG, "onCreateView: studentUid:$studentUid, subject=${subjectText}")
            docRef.get().addOnSuccessListener { documentSnapshot->
                if (documentSnapshot.exists()){
                    val totalAttendance = documentSnapshot["total_attendance"].toString()
                    Log.d(TAG, "onCreateView: TOTALATTENDANCE from db is:$totalAttendance")
                    if (totalAttendance.isNotEmpty()){
                        binding.tvTotalAttendance.text = totalAttendance.toString()
                    }
                }else{
                    Log.d(TAG, "onCreateView: Document not found for total attendance")
                }
                binding.progressbarStudentDetail.isVisible = false
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error:${it.message}", Toast.LENGTH_SHORT).show()
                binding.progressbarStudentDetail.isVisible = false
            }

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}