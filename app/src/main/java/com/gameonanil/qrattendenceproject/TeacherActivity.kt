package com.gameonanil.qrattendenceproject

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gameonanil.qrattendenceproject.databinding.ActivityTeacherBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class TeacherActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "TeacherActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            buttonGenerate.setOnClickListener {
                val bitmap = generateQRCode("Hello World!!")
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
}