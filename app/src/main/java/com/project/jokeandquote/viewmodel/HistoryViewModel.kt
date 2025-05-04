package com.project.jokeandquote.viewmodel

import android.app.Application
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.jokeandquote.model.HistoryRecord
import com.project.jokeandquote.service.HistoryDao
import kotlinx.coroutines.launch
import com.project.jokeandquote.service.PdfService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File
import com.project.jokeandquote.model.Resource
import kotlinx.coroutines.withContext


class HistoryViewModel(application: Application, private val pdfService: PdfService) : AndroidViewModel(application) {

    // MutableLiveData for internal updates
    val searchText = MutableLiveData<String>()
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private var searchJob: Job? = null

    private val _records = MutableLiveData<List<HistoryRecord>>()
    val records: LiveData<List<HistoryRecord>> = _records

    // For the history dialog
    private val _pdfGenerationResult = MutableLiveData<Resource<File>>()
    val pdfGenerationResult: LiveData<Resource<File>> get() = _pdfGenerationResult

    private val historyDao = HistoryDao(application)

    init {
        searchText.observeForever { query ->
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300)  // Debounce duration in milliseconds
                searchRecords(query)
            }
        }
    }

    private var allRecords: List<HistoryRecord> = listOf()

    fun loadAllRecords() {
        viewModelScope.launch {
            allRecords = historyDao.getAllRecords() // Fetch records from the database
            _records.value = allRecords // Update LiveData
        }
    }

    fun filterRecords(filter: String) {
        _records.value = when (filter) {
            "Quotation" -> allRecords.filter { it.type == "Quotation" }
            "Invoice" -> allRecords.filter { it.type == "Invoice" }
            else -> allRecords
        }
    }

    fun deleteRecord(record: HistoryRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            val filePath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "Records/${record.fileName}"
            )

            val fileDeleted = filePath.exists() && filePath.delete()

            if (fileDeleted) { Log.d("DeleteRecord", "File deleted successfully.")
            }

            // Delete from DB
            historyDao.delete(record)
            val updatedRecords = historyDao.getAllRecords()

            withContext(Dispatchers.Main) {
                _records.value = updatedRecords
                val resultMsg = if (fileDeleted) "Record and file deleted" else "Record deleted (file missing)"
                Toast.makeText(getApplication(), resultMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun searchRecords(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _records.value = if (query.isBlank()) {
                historyDao.getAllRecords()
            } else {
                historyDao.searchRecords(query)
            }
            _isLoading.value = false
        }
    }

    // Function to generate and share PDF
    fun generateAndSharePdf(record: HistoryRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Use public Documents/Records directory (external root)
                val publicDocsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Records")
                val file = File(publicDocsDir, record.fileName)

                if (file.exists()) {
                    _pdfGenerationResult.postValue(Resource.Success(file))

                } else {
                    val isGenerated = when (record.type) {
                        "Quotation" -> generateQuotationPdf(record)
                        "Invoice" -> generateInvoicePdf(record)
                        else -> false
                    }

                    if (isGenerated && file.exists()) {
                        _pdfGenerationResult.postValue(Resource.Success(file))
                    } else {
                        _pdfGenerationResult.postValue(Resource.Error("Failed to generate PDF"))
                    }
                }
            } catch (e: Exception) {
                _pdfGenerationResult.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    // Helper function to generate Quotation PDF
    private suspend fun generateQuotationPdf(record: HistoryRecord): Boolean {
        return pdfService.createQuotationPDF(
            comedianName = "String",
            comedianOfficeAddress = "String",
            comedianPhoneNumber = "String",
            comedianEmailAddress = "String",
            comedianBankName = "String",
            comedianAccountNumber = "String",
            comedianAccountType = "String",
            comedianNameOnAccount = "String",
            clientName = record.clientName ?: "",
            eventName = record.eventName ?: "",
            eventLocation = record.eventLocation ?: "",  // Default to empty string if null
            selectedDate = record.eventDate ?: "",       // Default to empty string if null
            selectedTime = record.eventTime ?: "",       // Default to empty string if null
            jobType = record.jobType ?: "",
            jobDuration = record.jobDuration ?: "",
            amountCharged = record.amountCharged ?: "",
            fileName = record.fileName,
            historyRecord = record,
        )
    }

    // Helper function to generate Invoice PDF
    private suspend fun generateInvoicePdf(record: HistoryRecord): Boolean {
        return pdfService.createInvoicePDF(
            comedianName = "String",
            comedianOfficeAddress = "String",
            comedianPhoneNumber = "String",
            comedianEmailAddress = "String",
            comedianBankName = "String",
            comedianAccountNumber = "String",
            comedianAccountType = "String",
            comedianNameOnAccount = "String",
            clientName = record.clientName ?: "",
            eventName = record.eventName ?: "",
            eventAddress = record.eventAddress ?: "",   // Default to empty string if null
            eventDate = record.eventDate ?: "",          // Default to empty string if null
            companyName = record.companyName ?: "",
            companyAddress = record.companyAddress ?: "",
            time = record.eventTime ?: "",               // Default to empty string if null
            jobType = record.jobType ?: "",
            jobDuration = record.jobDuration ?: "",
            amountCharged = record.amountCharged ?: "",
            fileName = record.fileName,
            historyRecord = record
        )
    }
}
