package com.gameonanil.qrattendenceproject.ui.teacher

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.adapter.AttendanceAdapter
import com.gameonanil.qrattendenceproject.databinding.FragmentMainTeacherBinding
import com.gameonanil.qrattendenceproject.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class MainTeacherFragment : Fragment() {
    companion object{
        private const val TAG = "MainTeacherFragment"
    }

    private var _binding: FragmentMainTeacherBinding? = null
    private val binding get()= _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AttendanceAdapter
    private lateinit var attendanceList: MutableList<User>
    private lateinit var teacherId: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainTeacherBinding.inflate(inflater, container, false)


        attendanceList = mutableListOf()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        adapter = AttendanceAdapter(requireActivity(),attendanceList)

        binding.recyclerTeacher.adapter = adapter

        teacherId = auth.currentUser!!.uid

        val collection = firestore.collection("attendance").document(teacherId).collection("student_list")
        collection.addSnapshotListener{snapshot,exception->
            if (exception !=null || snapshot == null){
                Log.e(TAG, "onCreate: Exception: $exception", )
                return@addSnapshotListener
            }


            val userFromDb = snapshot.toObjects(User::class.java)

            attendanceList.clear()
            attendanceList.addAll(userFromDb)
            adapter.notifyDataSetChanged()

        }


        binding.apply {
            fabTeacher.setOnClickListener {
                val action = MainTeacherFragmentDirections.actionMainTeacherFragmentToGeneratorFragment()
                findNavController().navigate(action)
            }
        }





        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}