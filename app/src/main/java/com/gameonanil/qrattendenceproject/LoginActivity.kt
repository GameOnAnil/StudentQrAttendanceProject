package com.gameonanil.qrattendenceproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.gameonanil.qrattendenceproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "LoginActivity"
    }

    private  lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.apply {
            btnLogin.setOnClickListener {
                if (etEmail.text.isEmpty() || etPass.text.isEmpty()){
                    Toast.makeText(this@LoginActivity, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                btnLogin.isEnabled = false
                progressbarLogin.isVisible = true

                val email = etEmail.text.toString().trim()
                val password = etPass.text.toString().trim()

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val currentUser = task.result?.user
                        val docRef = firebaseFirestore.collection("users").document(currentUser!!.uid)

                        docRef.get().addOnSuccessListener { docSnapshot->
                            val userTypeString = docSnapshot.data!!["user_type"]
                            Log.d(TAG, "onCreate: userTYpe = $userTypeString ")
                            when(userTypeString){
                                "student"->goToStudentActivity()
                                "teacher"->goToTeacherActivity()
                            }



                        }.addOnFailureListener {
                            btnLogin.isEnabled = true
                            progressbarLogin.isVisible = false
                            Toast.makeText(this@LoginActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        btnLogin.isEnabled = true
                        progressbarLogin.isVisible = false
                        Toast.makeText(this@LoginActivity, "Failed to login: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }


    }

    private fun goToTeacherActivity() {
        Toast.makeText(this@LoginActivity, "goToTeacherActivity", Toast.LENGTH_SHORT).show()
        binding.btnLogin.isEnabled = true
        binding.progressbarLogin.isVisible = false
        val intent = Intent(this,TeacherActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToStudentActivity() {
        Toast.makeText(this@LoginActivity, "goToStudentActivity", Toast.LENGTH_SHORT).show()
        binding.btnLogin.isEnabled = true
        binding.progressbarLogin.isVisible = false
        val intent = Intent(this,StudentActivity::class.java)
        startActivity(intent)
        finish()
    }
}