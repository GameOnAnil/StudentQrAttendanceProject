package com.gameonanil.qrattendenceproject.ui.teacher

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

import java.text.SimpleDateFormat
import java.util.*



class MainTeacherFragment : Fragment(), AttendanceAdapter.OnAttendanceClickListener {
    companion object {
        private const val TAG = "MainTeacherFragment"
    }

    private var _binding: FragmentMainTeacherBinding? = null
    private val binding get() = _binding!!

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
            setOf(
                R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarTeacherMain,
            navHostFragment,
            appBarConfiguration
        )


        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarTeacherMain)
        binding.toolbarTeacherMain.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }



        attendanceList = mutableListOf()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        adapter = AttendanceAdapter(requireActivity(), attendanceList, this)

        binding.recyclerTeacher.adapter = adapter

        teacherId = auth.currentUser!!.uid

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)


        val collection = firestore
            .collection("attendance")
            .document(teacherId)
            .collection("date")
            .document(formattedDate)
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


        binding.apply {
            fabTeacher.setOnClickListener {
                val action =
                    MainTeacherFragmentDirections.actionMainTeacherFragmentToGeneratorFragment()
                findNavController().navigate(action)
            }
        }

        return binding.root
    }

    //handling options menu
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

        if (item.itemId == R.id.itemSearchAttendance) {
            val action =
                MainTeacherFragmentDirections.actionMainTeacherFragmentToSearchAttendanceFragment()
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun handleItemClicked(position: Int, user: User) {
        Toast.makeText(
            activity,
            "Position Clicked= $position  Name = ${user.username}",
            Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "handleItemClicked: item clicked at $user")


        val action =
            MainTeacherFragmentDirections.actionMainTeacherFragmentToStudentsDetailFragment(user)
        findNavController().navigate(action)
    }

    override fun handleDeleteClicked(position: Int) {
        Log.d(TAG, "handleDeleteClicked: delete clicked!!!!!!!!!!!!!")
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)

        val collection = firestore
            .collection("attendance")
            .document(teacherId)
            .collection("date")
            .document(formattedDate)
            .collection("student_list")

        val currentUid = attendanceList[position].uid.toString().trim()

        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Are you sure you want to delete this student?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                collection.document(currentUid)
                    .delete()
                    .addOnSuccessListener {
                        adapter.notifyDataSetChanged()
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Student Deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error deleting document", e)
                    }
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()


    }


}