package com.gameonanil.qrattendenceproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gameonanil.qrattendenceproject.databinding.ActivityStudentBinding
import com.google.zxing.integration.android.IntentIntegrator


class StudentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}