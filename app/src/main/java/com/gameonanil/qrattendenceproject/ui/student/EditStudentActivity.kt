package com.gameonanil.qrattendenceproject.ui.student

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gameonanil.qrattendenceproject.databinding.ActivityEditStudentBinding
import com.gameonanil.qrattendenceproject.model.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class EditStudentActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EditStudentActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var studentUid: String
    private lateinit var binding: ActivityEditStudentBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEdit)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        studentUid = intent.getStringExtra("studentUid")!!
        Log.d(TAG, "onCreate:studentuID++++++$studentUid ")
        Toast.makeText(this, "studentuid:$studentUid", Toast.LENGTH_SHORT).show()
        initDetails(studentUid.toString())

        binding.apply {
            buttonAddTeacher.setOnClickListener {
                if (etUserName.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@EditStudentActivity,
                        "Please Enter User Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (etRoll.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@EditStudentActivity,
                        "Please Enter Roll",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                saveDetail(studentUid)
            }
        }


    }

    private fun saveDetail(studentUid: String) {
        val studentHashMap = HashMap<String, Any>()
        studentHashMap["username"] = binding.etUserName.text.toString()
        studentHashMap["roll"] = binding.etRoll.text.toString().toInt()
        if (binding.etAddress.text!!.isNotEmpty()) {
            studentHashMap["address"] = binding.etAddress.text!!.toString()
        }
        if (binding.etPhone.text!!.isNotEmpty()) {
            studentHashMap["phone"] = binding.etPhone.text!!.toString()
        }

        val collectionRef = firestore.collection("users")
        collectionRef.document(studentUid).update(studentHashMap)
            .addOnSuccessListener {

                //update attendance
                val attendanceRef = firestore
                    .collection("attendance")
                attendanceRef.get().addOnSuccessListener { QuerySnapshot ->
                    for (querySnapshot in QuerySnapshot) {
                        val currentTeacherUid = querySnapshot.id.toString()
                        modifyAttendance(currentTeacherUid,studentHashMap)

                    }
                }.addOnFailureListener {
                    Log.d(TAG, "saveDetail: FAILED AT modiflying attendance")
                }
                Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Error:${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun modifyAttendance(currentTeacherUid: String,userHashMap: HashMap<String,Any>) {
      val collectionRef =  firestore.collection("attendance")
        collectionRef.document(currentTeacherUid).collection("student_list").get()
            .addOnSuccessListener { QuerySnapshot->
                for (query in QuerySnapshot) {
                    if (query.reference.id==studentUid){
                        query.reference.update(userHashMap)
                    }
                }
                goToStartActivity()
            }.addOnFailureListener {
                Toast.makeText(this, "ERROR:${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun goToStartActivity(){
        finish()
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
            Toast.makeText(this, "ERROR:${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}