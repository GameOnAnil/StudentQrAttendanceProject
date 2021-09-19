package com.gameonanil.qrattendenceproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
 data class User(
    val uid: String? = "",
    val username: String? ="",
    val roll: String?="",
    val phone: String?="",
    val email: String? ="",
    val password: String?="",
    val user_type: String?=""

): Parcelable
