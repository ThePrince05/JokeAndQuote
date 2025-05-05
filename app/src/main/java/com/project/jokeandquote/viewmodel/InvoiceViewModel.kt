package com.project.jokeandquote.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.jokeandquote.model.HistoryRecord
import com.project.jokeandquote.service.HistoryDao
import com.project.jokeandquote.service.PdfService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceViewModel(application: Application) : AndroidViewModel(application){
    // MutableLiveData for internal updates
    val client = MutableLiveData<String>()
    val companyName = MutableLiveData<String>()
    val companyAddress = MutableLiveData<String>()
    val eventName = MutableLiveData<String>()
    val eventAddress = MutableLiveData<String>()
    val eventDate = MutableLiveData<String>()
    val eventTime = MutableLiveData<String>()
    val amountCharged = MutableLiveData<String>()

    val jobType = MutableLiveData<String>()
    val jobDuration = MutableLiveData<String>()

    private val historyDao = HistoryDao(application)

    var currentFileName: String = ""
        private set

    // Function to generate the PDF (use suspend to keep it asynchronous)
    suspend fun generateInvoicePDF(context: Context): Boolean {
        prepareFileName()
        val pdfService = PdfService(context)
        return  pdfService.createInvoicePDF(
            comedianName = "String",
            comedianOfficeAddress = "String",
            comedianPhoneNumber = "String",
            comedianEmailAddress = "String",
            comedianBankName = "String",
            comedianAccountNumber = "String",
            comedianAccountType = "String",
            comedianNameOnAccount = "String",
            client.value ?: "",
            eventName.value ?: "",
            eventAddress.value ?: "",
            eventDate.value ?: "",
            companyName.value ?: "",
            companyAddress.value ?: "",
            eventTime.value ?: "",
            jobType.value ?: "",
            jobDuration.value ?: "",
            amountCharged.value ?: "",
            currentFileName
        )
    }

    // Save the record to the database
    suspend fun saveRecordToDatabase() {
        viewModelScope.launch {
            try {
                val dateIssued = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

                val sdf = SimpleDateFormat("yyyy-MM-dd hh-mm-ss a", Locale.getDefault())
                sdf.format(Date())

                val record = HistoryRecord(
                    type = "Invoice",
                    dateIssued = dateIssued,
                    clientName = client.value.orEmpty(),
                    eventName = eventName.value.orEmpty(),
                    eventAddress = eventAddress.value.orEmpty(),
                    eventLocation = "",
                    eventDate = eventDate.value.orEmpty(),
                    eventTime = eventTime.value.orEmpty(),
                    companyName = companyName.value.orEmpty(),
                    companyAddress = companyAddress.value.orEmpty(),
                    jobType = jobType.value.orEmpty(),
                    jobDuration = jobDuration.value.orEmpty(),
                    amountCharged = amountCharged.value.orEmpty(),
                    fileName = currentFileName
                )
                historyDao.insertRecord(record)
            } catch (e: Exception) {
                Log.e("InvoiceViewModel", "Database insert failed", e)
            }
        }
    }

   private fun prepareFileName() {
        val formatter = SimpleDateFormat("yyyy-MM-dd hh-mm-ss a", Locale.getDefault())
        val timestamp = formatter.format(Date())
        currentFileName = "ComedianInvoice - $timestamp.pdf"
    }
}