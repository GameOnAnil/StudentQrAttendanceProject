package com.gameonanil.qrattendenceproject.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.adapter.AttendanceAdapter
import com.gameonanil.qrattendenceproject.databinding.FragmentMainTeacherBinding
import com.gameonanil.qrattendenceproject.model.User
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class MainTeacherFragment : Fragment(), AttendanceAdapter.OnAttendenceClickListener {
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
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainTeacherBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(binding.toolbarTeacherMain, navHostFragment,appBarConfiguration)


        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarTeacherMain)
        binding.toolbarTeacherMain.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }



        attendanceList = mutableListOf()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        adapter = AttendanceAdapter(requireActivity(),attendanceList,this)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun handleItemClicked(position: Int) {
        Toast.makeText(activity, "Position Clicked= $position", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "handleItemClicked: item clicked at $position")
    }

    override fun handleDeleteClicked(position: Int) {
        Log.d(TAG, "handleDeleteClicked: delete clicked!!!!!!!!!!!!!")
    }


}