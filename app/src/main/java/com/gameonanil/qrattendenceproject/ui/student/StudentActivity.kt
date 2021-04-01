package com.gameonanil.qrattendenceproject.ui.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.ActivityStudentBinding
import com.gameonanil.qrattendenceproject.model.User
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator


class StudentActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarStudent)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        collectionRef = firestore.collection("attendance")


        binding.apply {
            buttonScan.setOnClickListener{
                val scanner = IntentIntegrator(this@StudentActivity)
                scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                scanner.setPrompt("Scan a barcode")
                scanner.initiateScan()

            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode== Activity.RESULT_OK){
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_SHORT).show()
                    val teacherId = result.contents.toString()
                    addStudentToDb(teacherId)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun addStudentToDb(teacherId: String) {
        val currentUser = auth.currentUser

        firestore.collection("users").document(currentUser!!.uid).get().addOnSuccessListener {
            val userdata =  it.toObject(User::class.java)

            val docRef =  collectionRef
                .document(teacherId)
                .collection("student_list")
                .document(currentUser.uid)

            if (userdata != null) {
                docRef.set(userdata).addOnSuccessListener {
                    Toast.makeText(this@StudentActivity, "Student added successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }





    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_logout,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.itemLogout ->{
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

            else->{
                return super.onOptionsItemSelected(item)
            }

        }

    }
}