package com.gameonanil.qrattendenceproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.gameonanil.qrattendenceproject.databinding.ActivityTeacherBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class TeacherActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "TeacherActivity"
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarTeacher)

        auth = FirebaseAuth.getInstance()


        binding.apply {

            buttonGenerate.setOnClickListener {
                val teacherId = auth.currentUser!!.uid
                val bitmap = generateQRCode(teacherId)
                imageViewQR.setImageBitmap(bitmap)

            }

        }

    }

    private fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE,width,height)
            for (x in 0 until width){
                for (y in 0 until height){
                    bitmap.setPixel(x,y,if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                }
            }

        }catch (e: WriterException){
            Log.d(TAG, "generateQRCode: ${e.message}")
        }

        return bitmap

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_logout,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.itemLogout->{
                auth.signOut()
                val intent = Intent(this,LoginActivity::class.java)
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