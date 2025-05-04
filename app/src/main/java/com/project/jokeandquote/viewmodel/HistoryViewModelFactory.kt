package com.project.jokeandquote.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.jokeandquote.service.PdfService

class HistoryViewModelFactory(
    private val application: Application,
    private val pdfService: PdfService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(application, pdfService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
