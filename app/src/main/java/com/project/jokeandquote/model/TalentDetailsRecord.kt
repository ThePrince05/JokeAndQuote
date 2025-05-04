package com.project.jokeandquote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TalentDetailsRecord (
    val id: Long = 0,
    val name: String,
    val officeAddress: String,
    val phoneNumber: String,
    val emailAddress: String,
    val bankName: String,
    val accountNumber: String,
    val accountType: String,
    val nameOnAccount: String,
    val imageUrl: String? = null
): Parcelable