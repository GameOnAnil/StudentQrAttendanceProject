package com.gameonanil.qrattendenceproject.ui.teacher

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
import com.gameonanil.qrattendenceproject.adapter.AttendanceAdapter

import com.gameonanil.qrattendenceproject.adapter.NewAttendanceAdapter
import com.gameonanil.qrattendenceproject.databinding.FragmentMainTeacherBinding
import com.gameonanil.qrattendenceproject.databinding.FragmentNewAttendanceBinding
import com.gameonanil.qrattendenceproject.model.User
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NewAttendanceFragment : Fragment(),NewAttendanceAdapter.OnAttendanceClickListener {
    companion object{
        private const val TAG = "NewAttendanceFragment"
    }


    private var _binding: FragmentNewAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var adapter: NewAttendanceAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var attendanceList: MutableList<User>
    private lateinit var teacherId: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAttendanceBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarNewAttendance,
            navHostFragment,
            appBarConfiguration
        )


        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarNewAttendance)
        binding.toolbarNewAttendance.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }



        attendanceList = mutableListOf()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        adapter = NewAttendanceAdapter(requireActivity(), attendanceList, this)
        binding.recyclerNewList.adapter = adapter
        teacherId = auth.currentUser!!.uid


        val dateText = NewAttendanceFragmentArgs.fromBundle(requireArguments()).dateText
        getDataFromDb(dateText)

        return binding.root
    }

    private fun getDataFromDb(dateText: String){
        if (dateText.isNotEmpty()){
            binding.toolbarText.text = "Attendance at : $dateText"
        }


        val collection = firestore
            .collection("attendance")
            .document(teacherId)
            .collection("date")
            .document(dateText)
            .collection("student_list")

        collection.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "onCreate: Exception: $exception")
                return@addSnapshotListener
            }
            val userFromDb = snapshot.toObjects(User::class.java)

            attendanceList.clear()
            attendanceList.addAll(userFromDb)
            adapter.notifyDataSetChanged()

        }
    }


    override fun handleItemClicked(position: Int, user: User) {
        Log.d(TAG, "handleItemClicked: item clicked")
        TODO("Not yet implemented")
    }

    override fun handleDeleteClicked(position: Int) {
        Log.d(TAG, "handleDeleteClicked: item  trying to delete")
        TODO("Not yet implemented")
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


}