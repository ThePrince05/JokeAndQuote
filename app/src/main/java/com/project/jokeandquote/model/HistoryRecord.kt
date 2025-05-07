package com.project.jokeandquote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryRecord (
    val id: Long = 0,
    val type: String,   // "Invoice" or "Quotation"
    val dateIssued: String,
    val clientName: String,
    val eventName: String,
    val invoiceNumber: String?,
    val eventAddress: String?,
    val eventLocation: String?,
    val eventDate: String?,
    val eventTime: String?,
    val companyName: String?,
    val companyAddress: String?,
    val jobType: String,
    val jobDuration: String,
    val amountCharged: String,
    val fileName: String
): Parcelable