package com.project.jokeandquote.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.jokeandquote.R
import com.project.jokeandquote.model.HistoryRecord
import com.project.jokeandquote.model.Resource
import com.project.jokeandquote.service.PdfService
import com.project.jokeandquote.viewmodel.HistoryViewModel
import com.project.jokeandquote.viewmodel.HistoryViewModelFactory
import java.io.File
import java.util.Locale


class RecordDetailDialogFragment : DialogFragment() {
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var shareButton: Button
    private lateinit var loadingIndicator: ProgressBar

    companion object {
        private const val ARG_RECORD = "record_arg"

        fun newInstance(record: HistoryRecord): RecordDetailDialogFragment {
            val fragment = RecordDetailDialogFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARG_RECORD, record)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val record = requireArguments().getParcelable<HistoryRecord>(ARG_RECORD)!!

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_record_detail, null)
        val container = view.findViewById<LinearLayout>(R.id.detailsContainer)
        val title = view.findViewById<TextView>(R.id.documentTypeTitle)
        shareButton = view.findViewById(R.id.shareButton)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)

        title.text = record.type

        val pdfService = PdfService(requireContext().applicationContext)
        val factory = HistoryViewModelFactory(requireActivity().application, pdfService)
        historyViewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]

        shareButton.setOnClickListener {
            historyViewModel.generateAndSharePdf(record)
        }

        historyViewModel.pdfGenerationResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    showLoading(false)
                    result.data?.let { file -> sharePdf(file) }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading(true)
                }
            }
        })

        fun addRow(label: String, value: String?) {
            if (!value.isNullOrBlank()) {
                val themedContext = ContextThemeWrapper(context, com.google.android.material.R.style.Theme_Material3_DayNight)
                val row = TextView(themedContext).apply {
                    text = "$label: $value"
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                    setPadding(0, 8, 0, 8)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                }
                container.addView(row)
            }
        }


        when (record.type.lowercase(Locale.ROOT)) {
            "quotation" -> {
                addRow("Date Issued", record.dateIssued)
                addRow("Client Name", record.clientName)
                addRow("Event Name", record.eventName)
                addRow("Event Location", record.eventLocation)
                addRow("Event Date", record.eventDate)
                addRow("Event Time", record.eventTime)
                addRow("Amount Charged", "R${record.amountCharged}")
                addRow("Job Type", record.jobType)
                addRow("Job Duration", record.jobDuration)
            }
            "invoice" -> {
                addRow("Date Issued", record.dateIssued)
                addRow("Client Name", record.clientName)
                addRow("Company Name", record.companyName)
                addRow("Company Address", record.companyAddress)
                addRow("Event Name", record.eventName)
                addRow("Event Address", record.eventAddress)
                addRow("Event Date", record.eventDate)
                addRow("Event Time", record.eventTime)
                addRow("Amount Charged", "R${record.amountCharged}")
                addRow("Job Type", record.jobType)
                addRow("Job Duration", record.jobDuration)
            }
        }


        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()
    }




    private fun sharePdf(file: File) {
        // Share the PDF file
        Log.d("SharePDF", "Attempting to share: ${file.path}")
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.project.jokeandquote.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(Intent.createChooser(intent, "Share PDF"))

        // Optionally dismiss the dialog after sharing
        dismiss()
    }

    private fun showLoading(isLoading: Boolean) {
        // Show or hide the loading indicator
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}
