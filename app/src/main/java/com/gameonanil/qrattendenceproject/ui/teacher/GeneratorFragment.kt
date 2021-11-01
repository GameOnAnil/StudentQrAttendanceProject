package com.gameonanil.qrattendenceproject.ui.teacher

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.databinding.FragmentGeneratorBinding
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class GeneratorFragment : Fragment() {
    companion object{
        private const val TAG = "GeneratorFragment"
    }

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var subjectText: String
    private lateinit var teacherId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratorBinding.inflate(layoutInflater,container,false)

        val navHostFragment = NavHostFragment.findNavController(this);
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mainTeacherFragment,
            )
        )
        NavigationUI.setupWithNavController(binding.toolbarTeacherGenerate, navHostFragment,appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarTeacherGenerate)
        binding.toolbarTeacherGenerate.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        subjectText = GeneratorFragmentArgs.fromBundle(requireArguments()).subjectText
        teacherId = auth.currentUser!!.uid



        generateOnStart()

        return binding.root
    }

    private fun initAccessTrue(teacherId:String,semText:String){
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)

        val teacherDocRef = firestore.collection("attendance")
            .document("$teacherId,$subjectText,$formattedDate")
        val emptyHashMap = HashMap<String,String>()
        emptyHashMap["dummyField"] = ""

        /**JUST ADDING TEACHER ID FIELD WITH DUMMY TEXT**/
        teacherDocRef.set(emptyHashMap).addOnSuccessListener {
            /**NOW ADDING ACCESS FIELD**/
            val accessDocReference = firestore
                .collection("attendance")
                .document("$teacherId,$subjectText,$formattedDate")
                .collection("access")
                .document(teacherId)

            val accessHashMap = HashMap<String,Boolean>()
            accessHashMap["access_allowed"] = true
            accessDocReference.set(accessHashMap).addOnSuccessListener {
                Log.d(TAG, "setAccessFalse: ACCESS TRUE SUCESSFUL ")
            }.addOnFailureListener {
                Log.d(TAG, "setAccessFalse: ERROR:${it.message}")
            }
        }.addOnFailureListener {
            Log.d(TAG, "checkAccess: ERROR:${it.message}")
        }

    }

    private fun setAccessFalse(teacherId: String, semText: String){
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)
            val accessDocReference = firestore
            .collection("attendance")
            .document("$teacherId,$subjectText,$formattedDate")
            .collection("access")
            .document(teacherId)

        val accessHashMap = HashMap<String,Boolean>()
        accessHashMap["access_allowed"] = false
        accessDocReference.set(accessHashMap).addOnSuccessListener {
            Log.d(TAG, "setAccessFalse: ACCESS FALSE SUCESSFUL ")
        }.addOnFailureListener {
            Log.d(TAG, "setAccessFalse: ERROR:${it.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        if (teacherId.isNotEmpty()){
            initAccessTrue(teacherId,subjectText)
        }
    }

    override fun onStop() {
        super.onStop()
        if (teacherId.isNotEmpty()){
            setAccessFalse(teacherId,subjectText)
        }
    }

    private fun generateOnStart(){
        binding.apply {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyy.MM.dd")
            val formattedDate = formatter.format(date)
            val teacherId = auth.currentUser!!.uid
            val newTeacherId = "$teacherId/$subjectText/$formattedDate"
            val bitmap = generateQRCode(newTeacherId)
            imageViewQR.setImageBitmap(bitmap)
            Log.d(TAG, "generateOnStart: QR GENEREATED:$newTeacherId")


        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemLogout) {
            Log.d(TAG, "onOptionsItemSelected: logout pressed")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }


}