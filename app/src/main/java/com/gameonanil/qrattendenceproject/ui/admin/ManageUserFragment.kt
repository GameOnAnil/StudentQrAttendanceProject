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
        deleteFromUserCollection(currentUser)
        initLoadUsers()

    }

    private fun deleteFromUserCollection(currentUser: Users) {
        val collectionRef = firestore.collection("users")
        collectionRef.document(currentUser.uid.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteFromUserCollection: USER COLLECTION DELETED")
                if (currentUser.user_type == "student") {
                    deleteAttendanceCountByStudent(currentUser)
                    deleteStudentFromDB(currentUser.uid.toString())
                }
                if (currentUser.user_type == "teacher") {
                    deleteWholeFromAttendance(currentTeacherId = currentUser.uid.toString())
                    deleteAttendanceCountByTeacher(currentUser)
                }
            }.addOnFailureListener {
                Log.d(TAG, "FAILURE deleting usercollection:${it.message}")
            }
    }

    private fun deleteAttendanceCountByTeacher(currentUser: Users) {
        val collectionRef = firestore.collection("attendance_count")
        collectionRef.get().addOnSuccessListener { QuerySnapshot ->
            for (documentSnapshot in QuerySnapshot) {
                val subject = documentSnapshot.id.substringAfterLast(",")
                for (item in currentUser.subject!!) {
                    if (subject == item) {
                        documentSnapshot.reference.delete().addOnSuccessListener {
                            Log.d(TAG, "ATTENDANCE COUNT DELETE SUCCESS: ")
                        }.addOnFailureListener {
                            Log.d(TAG, "ATTENDANCE COUNT DELETE FAILURE:${it.message} ")
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Log.d(TAG, "FAIED TO GET ATTENDANCE COUNT COLLECTION:${it.message}")
        }
    }

    private fun deleteAttendanceCountByStudent(currentUser: Users) {
        val collectionRef = firestore.collection("attendance_count")
        collectionRef.get().addOnSuccessListener { QuerySnapshot ->
            for (documentSnapshot in QuerySnapshot) {
                val studentUid = documentSnapshot.id.substringBefore(",")
                if (studentUid == currentUser.uid) {
                    documentSnapshot.reference.delete().addOnSuccessListener {
                        Log.d(TAG, "ATTENDANCE COUNT DELETE SUCCESS: ")
                    }.addOnFailureListener {
                        Log.d(TAG, "ATTENDANCE COUNT DELETE FAILURE:${it.message} ")
                    }
                }
            }

        }.addOnFailureListener {
            Log.d(TAG, "FAIED TO GET ATTENDANCE COUNT COLLECTION:${it.message}")
        }
    }

    private fun deleteStudentFromDB(currentUserUID: String) {
        val collectionRef = firestore.collection("attendance")
        collectionRef.get().addOnSuccessListener { QuerySnapshot ->
            for (documentSnapshot in QuerySnapshot) {
                documentSnapshot.reference.collection("student_list")
                    .document(currentUserUID).delete()
                    .addOnSuccessListener { Log.d(TAG, "Student deleted successfully") }
                    .addOnFailureListener { Log.d(TAG, "deleteStudentFromDB: ${it.message}") }
            }
        }.addOnFailureListener {
            Log.d(TAG, "deleteStudentFromDB: ERROR:${it.message}")
        }
    }

    private fun deleteWholeFromAttendance(currentTeacherId: String) {
        val collectionRef = firestore
            .collection("attendance")
        collectionRef.get().addOnSuccessListener { QuerySnapshot ->
            for (querySnapshot in QuerySnapshot) {
                querySnapshot.reference.update("teacher", "")
                Log.d(TAG, "deleteTeacherFromAttendance: teacherid=${querySnapshot.id}")
                val extractedTeacherId = querySnapshot.id.substringBefore(",")
                Log.d(TAG, "EXTRACTED TEACHERID:$extractedTeacherId and UID:${currentTeacherId}")
                if (currentTeacherId == extractedTeacherId) {
                    deleteAccessDocument(querySnapshot.id)
                    deleteSubjectListDocument(querySnapshot.id)
                }
            }
        }.addOnFailureListener {
            Log.d(TAG, "FAILED TO GET TEACHER DOC.:${it.message}")
        }

    }

    private fun deleteAccessDocument(uniqueTeacherId: String) {
        val collectionRef = firestore
            .collection("attendance")
            .document(uniqueTeacherId)
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

    private fun deleteSubjectListDocument(uniqueTeacherId: String) {
        val collectionRef = firestore
            .collection("attendance")
            .document(uniqueTeacherId)
            .collection("student_list")

        collectionRef.get().addOnSuccessListener { querySnapshot ->
            for (query in querySnapshot) {
                Log.d(TAG, "DELETING STUDENTLIST: UID:${query.id}")
                query.reference.delete()
            }
            deleteTeacherUidDoc(uniqueTeacherId)
        }
    }

    private fun deleteTeacherUidDoc(uniqueTeacherId: String) {
        val docRef = firestore
            .collection("attendance")
            .document(uniqueTeacherId)
        docRef.delete().addOnSuccessListener {
            Log.d(TAG, "deleteTeacherUidDoc: Main Teacher deleted succcesfully")
        }.addOnFailureListener {
            Log.d(TAG, "deleteTeacherUidDoc: Error:${it.message}")
        }
    }


}