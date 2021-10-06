package com.gameonanil.qrattendenceproject.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentSemBinding
import com.gameonanil.qrattendenceproject.model.Teacher
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SubjectFragment : Fragment() {
    companion object {
        private const val TAG = "SemFragment"
    }

    private var _binding: FragmentSemBinding? = null
    private val binding get() = _binding!!
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
   private lateinit var subjectText: String
    private var subjectType: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.semFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarSem,
            navHostFragment,
            appBarConfiguration
        )

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarSem)
        binding.toolbarSem.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        subjectText = ""


        loadSemester()

        binding.apply {
            autoCompleteSemester.inputType = EditorInfo.TYPE_NULL
            buttonSemGo.setOnClickListener {
                goToNextPage()
            }
        }


        return binding.root
    }

    private fun loadSemester() {
        val teacherId = mAuth.currentUser!!.uid
        val teacherDocRef = firestore.collection("users").document(teacherId)

        teacherDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentUser = documentSnapshot.toObject(Teacher::class.java)
                    subjectType = currentUser!!.subject
                    Log.d(TAG, "loadSemester: semType:$subjectType")

                  //  val semTypes = resources.getStringArray(R.array.sem_temp)
                    val semArrayList:ArrayList<String>  = ArrayList()
                    for (currentSub in subjectType!!){
                        semArrayList.add(currentSub)
                    }
                    if (semArrayList.isNotEmpty()){
                        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, semArrayList)
                        binding.autoCompleteSemester.setAdapter(arrayAdapter)
                    }
                }
            }.addOnFailureListener {
                Log.d(TAG, "loadSemester: Error:${it.message}")
                Toast.makeText(requireContext(), "Failed to load Sem:${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

   private fun goToNextPage() {
        subjectText = binding.autoCompleteSemester.text.toString()

       if (subjectText==""||subjectText=="Select Subject"){
           Toast.makeText(requireContext(), "Subject Empty", Toast.LENGTH_SHORT).show()
       }else{
           val action = SubjectFragmentDirections.actionSemFragmentToMainTeacherFragment(subjectText)
           findNavController().navigate(action)
       }

    }

    /**SETTING UP DROPDOWN MENU **/
    override fun onResume() {
        super.onResume()
       // loadSemester()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

}