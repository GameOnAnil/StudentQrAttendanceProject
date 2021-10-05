package com.gameonanil.qrattendenceproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    val uid: String? = "",
    val email: String? ="",
    val username: String? ="",
    val roll: Int?=null,
    val address: String?="",
    val phone: String?="",
    val user_type: String?="",


): Parcelable
