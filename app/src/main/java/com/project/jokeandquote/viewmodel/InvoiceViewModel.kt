package com.project.jokeandquote.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.jokeandquote.model.HistoryRecord
import com.project.jokeandquote.model.TalentDetailsRecord
import com.project.jokeandquote.service.HistoryDao
import com.project.jokeandquote.service.PdfService
import com.project.jokeandquote.service.TalentDetailsDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceViewModel(application: Application) : AndroidViewModel(application){
    private val dao = TalentDetailsDao(application)
    val talentDetails = MutableLiveData<TalentDetailsRecord?>()

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
    val comedianName = MutableLiveData("")
    val comedianOfficeAddress = MutableLiveData("")
    val comedianPhoneNumber = MutableLiveData("")
    val comedianEmailAddress = MutableLiveData("")
    val comedianBankName = MutableLiveData("")
    val comedianAccountNumber = MutableLiveData("")
    val comedianAccountType = MutableLiveData("")
    val comedianNameOnAccount = MutableLiveData("")
    val logoUri = MutableLiveData("")

    private val historyDao = HistoryDao(application)

    init {
        loadTalentDetails()
    }

    var currentFileName: String = ""
        private set

    // Function to generate the PDF (use suspend to keep it asynchronous)
    suspend fun generateInvoicePDF(context: Context): Boolean {
        prepareFileName()
        val pdfService = PdfService(context)
        return  pdfService.createInvoicePDF(
            comedianName = comedianName.value ?: "",
            comedianOfficeAddress = comedianOfficeAddress.value ?: "",
            comedianPhoneNumber = comedianPhoneNumber.value ?: "",
            comedianEmailAddress = comedianEmailAddress.value ?: "",
            comedianBankName = comedianBankName.value ?: "",
            comedianAccountNumber = comedianAccountNumber.value ?: "",
            comedianAccountType = comedianAccountType.value ?: "",
            comedianNameOnAccount = comedianNameOnAccount.value ?: "",
            logoUri = logoUri.value ?: "",
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
                val currentDate = Date()

                val dateIssued = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(currentDate)
                val timestampFormat = SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault())
                val invoiceNumber = comedianName.value.toString().sliceAndCapitalize() + timestampFormat.format(currentDate)

                val record = HistoryRecord(
                    type = "Invoice",
                    dateIssued = dateIssued,
                    invoiceNumber = invoiceNumber,
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

    private fun loadTalentDetails() {
        viewModelScope.launch {
            val details = dao.getRecords().firstOrNull()
            talentDetails.postValue(details)
            details?.let {
                comedianName.postValue(it.name ?: "")
                comedianOfficeAddress.postValue(it.officeAddress ?: "")
                comedianPhoneNumber.postValue(it.phoneNumber ?: "")
                comedianEmailAddress.postValue(it.emailAddress ?: "")
                comedianBankName.postValue(it.bankName ?: "")
                comedianAccountNumber.postValue(it.accountNumber ?: "")
                comedianAccountType.postValue(it.accountType ?: "")
                comedianNameOnAccount.postValue(it.nameOnAccount ?: "")
                logoUri.postValue(it.logoUri ?: "")
            }
        }
    }

    fun String.sliceAndCapitalize(): String {
        return this.take(3).uppercase()
    }
}

