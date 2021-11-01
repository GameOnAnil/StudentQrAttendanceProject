package com.gameonanil.qrattendenceproject.ui.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gameonanil.qrattendenceproject.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "ForgetPasswordActivity"
    }
    
    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var emailString: String
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarReset)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()

        binding.toolbarReset.setNavigationOnClickListener {
            finish()
        }
        binding.buttonSendReset.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        emailString = binding.etEmail.text!!.trim().toString()
        if (emailString.isEmpty()) {
            Toast.makeText(baseContext, "Email Required", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "validateData: emailString empty")
        } else {
            forgotPassword()
        }
    }

    private fun forgotPassword() {
        mAuth.sendPasswordResetEmail(emailString)
            .addOnSuccessListener {
                Toast.makeText(this, "Reset Email Sent Successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "forgotPassword: Reset Successful")
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Error:${it.message}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "forgotPassword: Error:${it.message}")
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}