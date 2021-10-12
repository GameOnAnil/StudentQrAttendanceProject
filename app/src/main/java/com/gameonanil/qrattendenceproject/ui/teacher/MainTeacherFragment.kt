package com.gameonanil.qrattendenceproject.ui.teacher


import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.qrattendenceproject.R
import com.gameonanil.qrattendenceproject.adapter.AttendanceAdapter
import com.gameonanil.qrattendenceproject.databinding.FragmentMainTeacherBinding
import com.gameonanil.qrattendenceproject.model.Student
import com.gameonanil.qrattendenceproject.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFFont
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainTeacherFragment : Fragment(), AttendanceAdapter.OnAttendanceClickListener {
    companion object {
        private const val TAG = "MainTeacherFragment"
    }

    private var _binding: FragmentMainTeacherBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AttendanceAdapter
    private lateinit var attendanceList: MutableList<Student>
    private lateinit var teacherId: String
    private lateinit var subjectText: String
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var cell: Cell? = null
    private var row: Row? = null
    private lateinit var defaultStyle: CellStyle
    private lateinit var currentDate: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainTeacherBinding.inflate(inflater, container, false)

        /**Setting Up Toolbar*/
        val navHostFragment = NavHostFragment.findNavController(this);

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.semFragment,
            )
        )
        NavigationUI.setupWithNavController(
            binding.toolbarTeacherMain,
            navHostFragment,
            appBarConfiguration
        )


        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarTeacherMain)
        binding.toolbarTeacherMain.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        subjectText = MainTeacherFragmentArgs.fromBundle(requireArguments()).subjectText

        attendanceList = mutableListOf()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        adapter = AttendanceAdapter(requireActivity(), attendanceList, this)

        binding.recyclerTeacher.adapter = adapter
        teacherId = auth.currentUser!!.uid

        initListFromDb()


        binding.apply {
            fabTeacher.setOnClickListener {
                val action =
                    MainTeacherFragmentDirections.actionMainTeacherFragmentToGeneratorFragment(
                        subjectText
                    )
                findNavController().navigate(action)
            }

            buttonDownloadAttendance.setOnClickListener {
                if (attendanceList.isNotEmpty()) {
                    handleDownloadAttendance()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No Attendance to Download",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }


        }

        return binding.root
    }

    private fun initListFromDb() {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)
        currentDate = formattedDate

        val collection = firestore
            .collection("attendance")
            .document("$teacherId,$subjectText,$formattedDate")
            .collection("student_list")

        collection.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "onCreate: Exception: $exception")
                return@addSnapshotListener
            }


            val userFromDb = snapshot.toObjects(Student::class.java)
            attendanceList.clear()
            attendanceList.addAll(userFromDb)
            attendanceList.sortBy { it.roll!! }
            adapter.notifyDataSetChanged()

        }
    }


    //handling options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
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

        if (item.itemId == R.id.itemSearchAttendance) {
            val action =
                MainTeacherFragmentDirections.actionMainTeacherFragmentToSearchAttendanceFragment(
                    subjectText
                )
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleDownloadAttendance() {
        val wb = HSSFWorkbook()
        val sheet = wb.createSheet()

        val defaultFont: HSSFFont = wb.createFont()
        defaultFont.fontHeightInPoints = 10.toShort()
        defaultFont.fontName = "Arial"
        defaultFont.color = IndexedColors.BLACK.index
        defaultFont.boldweight = HSSFFont.BOLDWEIGHT_BOLD


        val cellStyle1: CellStyle = wb.createCellStyle()
        cellStyle1.fillForegroundColor = HSSFColor.AQUA.index
        cellStyle1.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle1.alignment = CellStyle.ALIGN_CENTER
        cellStyle1.wrapText
        cellStyle1.setFont(defaultFont)

        val cellStyle2: CellStyle = wb.createCellStyle()
        cellStyle2.alignment = CellStyle.ALIGN_CENTER
        cellStyle2.wrapText

        row = sheet.createRow(0)
        defaultStyle = cellStyle1
        cell = row!!.createCell(0);
        cell?.setCellValue("Roll")
        cell?.cellStyle = defaultStyle

        cell = row!!.createCell(1);
        cell?.setCellValue("Student Name")
        cell?.cellStyle = defaultStyle

        cell = row!!.createCell(2);
        cell?.setCellValue("Phone")
        cell?.cellStyle = defaultStyle

        cell = row!!.createCell(3);
        cell?.setCellValue("Email")
        cell?.cellStyle = defaultStyle

        defaultStyle = cellStyle2
        for (i in attendanceList.indices) {
            row = sheet.createRow(i + 1)

            cell = row!!.createCell(0);
            cell?.setCellValue(attendanceList[i].roll.toString())
            cell?.cellStyle = defaultStyle

            cell = row!!.createCell(1);
            cell?.setCellValue(attendanceList[i].username)
            cell?.cellStyle = defaultStyle

            cell = row!!.createCell(2);
            cell?.setCellValue(attendanceList[i].phone)
            cell?.cellStyle = defaultStyle

            cell = row!!.createCell(3);
            cell?.setCellValue(attendanceList[i].email)
            cell?.cellStyle = defaultStyle


        }

        sheet.setColumnWidth(0, 3600)
        sheet.setColumnWidth(1, 5500)
        sheet.setColumnWidth(2, 3600)
        sheet.setColumnWidth(3, 8500)


        val fos: OutputStream
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(TAG, "createExcel: new way called")
                val resolver = requireActivity().applicationContext.contentResolver
                val contentValues = ContentValues()
                contentValues.put(
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    "Attendance_$currentDate.xls"
                )
                contentValues.put(
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    "application/vnd.ms-excel"
                )
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + File.separator + "Attendance"
                )
                val excelUri = resolver.insert(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    contentValues
                )
                fos = resolver.openOutputStream(Objects.requireNonNull(excelUri)!!)!!
                wb.write(fos)
                Toast.makeText(requireContext(), "EXCEL File Saved", Toast.LENGTH_SHORT).show()
            } else {

                val file = File(
                    requireActivity().applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "Attendance_$currentDate.xls"
                )
                val outputStream = FileOutputStream(file)
                wb.write(outputStream)

                Log.d(TAG, "createExcel: Old way called")
                Toast.makeText(requireContext(), "Excel File Stored", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (io: IOException) {
            Toast.makeText(requireContext(), "Error:${io.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decreaseTotalAttendance(studentId: String) {
        Log.d(TAG, "decreaseTotalAttendance: deletetotalatt called!!!")
        val studentDocRef = firestore.collection("attendance_count")
            .document(studentId)
            .collection("subject")
            .document(subjectText)
        Log.d(TAG, "increaseTotalAttendance: docRef=${studentDocRef.path}")

        studentDocRef.get().addOnCompleteListener { docSnapshot ->
            /** When student subject attendance count exists**/
            if (docSnapshot.result!!.exists()) {
                Log.d(TAG, "decreaseTotalAttendance:  EXIST")
                val totalAttendance =
                    docSnapshot.result!!["total_attendance"].toString().toInt()
                if (totalAttendance > 0) {
                    studentDocRef.update("total_attendance", FieldValue.increment(-1))
                    Log.d(TAG, "decreaseTotalAttendance: UPDATED!!!!!!!")
                }

            } else {
                Log.d(TAG, "increaseTotalAttendance: Error:Document Doesnt Exixt")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun handleItemClicked(position: Int, user: Student) {
        val action =
            MainTeacherFragmentDirections.actionMainTeacherFragmentToStudentsDetailFragment(
                user,
                subjectText
            )
        findNavController().navigate(action)
    }

    override fun handleDeleteClicked(position: Int) {
        Log.d(TAG, "handleDeleteClicked: delete clicked!!!!!!!!!!!!!")
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = formatter.format(date)

        val collection = firestore
            .collection("attendance")
            .document("$teacherId,$subjectText,$formattedDate")
            .collection("student_list")

        val currentUid = attendanceList[position].uid.toString().trim()

        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Are you sure you want to delete this student?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                collection.document(currentUid)
                    .delete()
                    .addOnSuccessListener {
                        adapter.notifyDataSetChanged()
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Student Deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        decreaseTotalAttendance(currentUid)

                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error deleting document", e)
                    }
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()


    }


}