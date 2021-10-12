package com.gameonanil.qrattendenceproject.ui.admin

import android.content.ContentValues.TAG
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
import com.gameonanil.qrattendenceproject.adapter.ManageUserAdapter
import com.gameonanil.qrattendenceproject.databinding.FragmentManageUserBinding
import com.gameonanil.qrattendenceproject.model.Users
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ManageUserFragment : Fragment(), ManageUserAdapter.OnUserClickListener {
    private var _binding: FragmentManageUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAdapter: ManageUserAdapter
    private lateinit var mUserList: MutableList<Users>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentManageUserBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainAdminFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarManageUser,
            navHostFragment,
            appBarConfiguration
        )

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarManageUser)
        binding.toolbarManageUser.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }


        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        mUserList = mutableListOf()

        mAdapter = ManageUserAdapter(requireContext(), mUserList, this)

        binding.recyclerManageUser.adapter = mAdapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initLoadUsers()
    }


    private fun initLoadUsers() {
        mUserList.clear()
        val collectionReference = firestore.collection("users")
        collectionReference.get().addOnSuccessListener { querySnapshot ->
            for (query in querySnapshot) {
                val currentUser = query.toObject(Users::class.java)
                if (currentUser.user_type != "admin") {
                    mUserList.add(currentUser)
                }
            }
            mAdapter.notifyDataSetChanged()

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error:${it.message}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "initLoadUsers: Error:${it.message}")
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

    override fun handleItemClicked(position: Int, user: Users) {
        TODO("Not yet implemented")
    }

    override fun handleDeleteClicked(position: Int) {
        val currentUser = mUserList[position]
        // deleteFromUserCollection(currentUser)
        if (currentUser.user_type == "student") {
            Log.d(TAG, "deleteFromUserCollection: usertype=${currentUser.user_type}")
            deleteFromAttendanceCount(currentUser)
        }

        deleteTeacherFromAttendance(currentUser)

    }

    private fun deleteFromUserCollection(currentUser: Users) {
        val collectionRef = firestore.collection("users")
        collectionRef.document(currentUser.uid.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteFromUserCollection: USER COLLECTION DELETED")
            }.addOnFailureListener {
                Log.d(TAG, "FAILURE deleting usercollection:${it.message}")
            }
    }

    private fun deleteFromAttendanceCount(currentUser: Users) {
        val collectionRef = firestore.collection("attendance_count")

        collectionRef.document(currentUser.uid.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteFromUserCollection: ATTENDANCE_COUNT COLLECTION DELETED")
            }.addOnFailureListener {
                Log.d(TAG, "FAILURE deleting usercollection:${it.message}")
            }
    }

    private fun deleteTeacherFromAttendance(currentUser: Users) {
        deleteAccessDocument("fozXgmAxuUZjts2cHr0TzzwT7HB2", "C", "2021.10.06")
        deleteSubjectListDocument("fozXgmAxuUZjts2cHr0TzzwT7HB2", "C", "2021.10.06")
        deleteDateDocument("fozXgmAxuUZjts2cHr0TzzwT7HB2", "C")

    }

    private fun deleteAccessDocument(teacherId: String, subject: String, date: String) {
        val collectionRef = firestore
            .collection("attendance")
            .document(teacherId)
            .collection("subject")
            .document(subject)
            .collection("date")
            .document(date)
            .collection("access")

        collectionRef.get().addOnSuccessListener { querySnapshot ->
            for (query in querySnapshot) {
                Log.d(TAG, "DELETING ACCESS: UID:${query.id}")
                query.reference.delete().addOnSuccessListener {
                    Log.d(
                        TAG,
                        "deleteAccessDocument: DELETION SUCCESSFUL"
                    )
                }.addOnFailureListener {
                    Log.d(TAG, "ERROR IN DELETING DOC:${it.message}")
                }
            }
        }
    }

    private fun deleteSubjectListDocument(teacherId: String, subject: String, date: String) {
        val collectionRef = firestore
            .collection("attendance")
            .document(teacherId)
            .collection("subject")
            .document(subject)
            .collection("date")
            .document(date)
            .collection("student_list")

        collectionRef.get().addOnSuccessListener { querySnapshot ->
            for (query in querySnapshot) {
                Log.d(TAG, "DELETING STUDENTLIST: UID:${query.id}")
                query.reference.delete()
            }
        }
    }

    private fun deleteDateDocument(teacherId: String, subject: String) {
        val collectionRef = firestore
            .collection("attendance")
            .document(teacherId)
            .collection("subject")
            .document(subject)
            .collection("date")

        collectionRef.get().addOnSuccessListener { querySnapshot ->
            for (query in querySnapshot) {
                Log.d(TAG, "DELETING DATE: UID:${query.id}")
                query.reference.delete()
            }
        }
    }
}