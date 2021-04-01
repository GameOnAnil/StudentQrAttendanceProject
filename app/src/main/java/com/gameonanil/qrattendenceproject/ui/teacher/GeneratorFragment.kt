package com.gameonanil.qrattendenceproject.ui.teacher

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentGeneratorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException


class GeneratorFragment : Fragment() {
    companion object{
        private const val TAG = "GeneratorFragment"
    }

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratorBinding.inflate(layoutInflater,container,false)

        auth = FirebaseAuth.getInstance()

        binding.apply {

            buttonGenerate.setOnClickListener {
                val teacherId = auth.currentUser!!.uid
                val bitmap = generateQRCode(teacherId)
                imageViewQR.setImageBitmap(bitmap)

            }

        }


        return binding.root
    }

    private fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}