package com.gameonanil.qrattendenceproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Teacher(
    val uid: String? = "",
    val username: String? ="",
    val address: String?="",
    val phone: String?="",
    val email: String? ="",
    val user_type: String?="",
    val subject:List<String>?=null,
): Parcelable