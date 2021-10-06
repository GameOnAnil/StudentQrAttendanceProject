package com.gameonanil.qrattendenceproject.ui.teacher

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
import com.gameonanil.qrattendenceproject.databinding.FragmentSemBinding
import com.gameonanil.qrattendenceproject.model.Teacher
import com.gameonanil.qrattendenceproject.ui.admin.AddTeacherFragment
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
    private lateinit var semArrayList:ArrayList<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>

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

        binding.apply {

            buttonSemGo.setOnClickListener {
                goToNextPage()
            }
            buttonAddSub.setOnClickListener {
                handleAddSubjectClicked()

            }

            buttonRemove.setOnClickListener {
                val currentText = binding.spinner.selectedItem.toString()
                if (currentText!=""){
                    semArrayList.remove(currentText)
                    updateSubjectInDB(false)
                    spinnerAdapter.notifyDataSetChanged()
                }
            }
        }


        return binding.root
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
                    semArrayList.add(editText.text.toString())
                    updateSubjectInDB(true)
                    spinnerAdapter.notifyDataSetChanged()
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

    private fun updateSubjectInDB(isInsert:Boolean){
        val teacherId = mAuth.currentUser!!.uid
        val teacherDocRef = firestore.collection("users").document(teacherId)

        teacherDocRef.update("subject",semArrayList).addOnSuccessListener {
            if (isInsert==true){
                Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "Error Updating Subject:${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
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
                    semArrayList = ArrayList()
                    for (currentSub in subjectType!!){
                        semArrayList.add(currentSub)
                    }
                    if (semArrayList.isNotEmpty()){
                        spinnerAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, semArrayList)
                        binding.spinner.setAdapter(spinnerAdapter)
                    }
                }
            }.addOnFailureListener {
                Log.d(TAG, "loadSemester: Error:${it.message}")
                Toast.makeText(requireContext(), "Failed to load Sem:${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

   private fun goToNextPage() {
        subjectText = binding.spinner.selectedItem.toString()
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
        loadSemester()

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