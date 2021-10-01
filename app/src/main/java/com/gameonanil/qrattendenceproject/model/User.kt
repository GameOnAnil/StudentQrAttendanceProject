package com.gameonanil.qrattendenceproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
 data class User(
    val uid: String? = "",
    val username: String? ="",
    val roll: Int?=null,
    val address: String?="",
    val phone: String?="",
    val email: String? ="",
    val password: String?="",
    val user_type: String?="",
    val subject:List<String>?=null,

    val semester:List<String>?=null

): Parcelable
