package com.gameonanil.qrattendenceproject.ui.student

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.ActivityStudentBinding
import com.gameonanil.qrattendenceproject.model.User
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*


class StudentActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "StudentActivity"
    }

    private lateinit var auth: FirebaseAuth

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference
    private lateinit var currentUid: String
    private lateinit var binding: ActivityStudentBinding
    private lateinit var semesterTestFromQr : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarStudent)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        collectionRef = firestore.collection("attendance")
        currentUid = auth.currentUser!!.uid


        binding.apply {
            buttonScan.setOnClickListener {
                val scanner = IntentIntegrator(this@StudentActivity)
                scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                scanner.setPrompt("Scan a barcode")
                scanner.initiateScan()
            }
        }
        binding.progressbarStudent.isVisible = true
        displayStudentDetail()

       

    }

    private fun displayStudentDetail() {
        val userDocRef = firestore.collection("users").document(currentUid)
        userDocRef.get().addOnSuccessListener { docSnapshot->
            if (docSnapshot.exists()){
                val userDetail = docSnapshot.toObject(User::class.java)
                if (userDetail != null) {
                    userDetail.username?.let { binding.tvUserName.text = it }
                    userDetail.email?.let { binding.tvEmail.text = it }
                    userDetail.roll?.let { binding.tvRoll.text = it }
                    userDetail.address?.let { binding.tvAddress.text = it }
                    userDetail.phone?.let { binding.tvPhone.text = it }
                }
            }

            binding.progressbarStudent.isVisible = false

        }.addOnFailureListener {
            Toast.makeText(this, "Error:${it.message}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "displayStudentDetail: Error:${it.message}")
            binding.progressbarStudent.isVisible = false
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "onActivityResult: Scanned:${result.contents}")
                    val teacherIdPlusSem = result.contents.toString()
                    semesterTestFromQr = getLastNCharsOfString(teacherIdPlusSem,3)
                    val newTeacherId = teacherIdPlusSem.dropLast(3)
                    Toast.makeText(this, "Teacher Id:$newTeacherId and Sem=$semesterTestFromQr", Toast.LENGTH_SHORT).show()
                    addStudentToDb(newTeacherId,semesterTestFromQr)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    fun getLastNCharsOfString(str: String, n: Int): String {
        var lastnChars = str
        if (lastnChars.length > n) {
            lastnChars = lastnChars.substring(lastnChars.length - n, lastnChars.length)
        }
        return lastnChars
    }

    private fun addStudentToDb(teacherId: String,semText:String) {

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)

        firestore.collection("users").document(currentUid).get().addOnSuccessListener {
            val userdata = it.toObject(User::class.java)

            val docRef = collectionRef
                .document(teacherId)
                .collection("semester")
                .document(semesterTestFromQr)
                .collection("date")
                .document(formattedDate.toString())
                .collection("student_list")
                .document(currentUid)

            docRef.get().addOnCompleteListener { documentSnapshot ->
                if (documentSnapshot.result!!.exists()) {
                    Toast.makeText(this, "Student Already Added", Toast.LENGTH_SHORT).show()
                } else {
                    userdata?.let {
                        docRef.set(userdata).addOnSuccessListener {
                            Toast.makeText(
                                this@StudentActivity,
                                "Student added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "addStudentToDb: ADD CALLED!!!!!!!!!")
                            increaseTotalAttendance(teacherId)
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error:${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }


    }

    private fun increaseTotalAttendance(teacherId: String) {
        val teacherReference = firestore.collection("users").document(teacherId)
        teacherReference.get().addOnSuccessListener {
            val currentTeacher = it.toObject(User::class.java)
            val subject = currentTeacher!!.subject
            Log.d(TAG, "increaseTotalAttendance: Got teacher and subject=$subject")
            if (subject!!.isNotEmpty()) {
                val studentDocRef = firestore.collection("student")
                    .document(currentUid)
                    .collection("subject")
                    .document(subject.trim())
                Log.d(TAG, "increaseTotalAttendance: docRef=${studentDocRef.path}")

                studentDocRef.get().addOnCompleteListener { docSnapshot ->
                    /** When student subject attendance count exists**/
                    if (docSnapshot.result!!.exists()) {
                        studentDocRef
                            .update("total_attendance", FieldValue.increment(1))
                            .addOnSuccessListener {
                                Log.d(TAG, "increaseTotalAttendance: Totalattendance updated")
                            }
                            .addOnFailureListener {
                                Log.d(
                                    TAG,
                                    "increaseTotalAttendance: totalattendance not updated:${it.message}"
                                )
                            }
                    }
                    /** When student subject attendance count Doesn't exists**/
                    else {
                        val attendanceHashMap = hashMapOf<String, Int>("total_attendance" to 1)
                        studentDocRef.set(attendanceHashMap).addOnSuccessListener {
                            Log.d(TAG, "increaseTotalAttendance: new created")
                        }.addOnFailureListener {
                            Log.d(TAG, "increaseTotalAttendance: Error:${it.message}")
                        }

                    }
                }


            } else {
                Log.d(TAG, "increaseTotalAttendance: Error: Coundn't find subject")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error:${it.message}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_logout, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemLogout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}