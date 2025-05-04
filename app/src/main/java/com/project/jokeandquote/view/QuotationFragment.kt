package com.project.jokeandquote.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.project.jokeandquote.R
import com.project.jokeandquote.databinding.FragmentQuotationBinding
import com.project.jokeandquote.viewmodel.QuotationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuotationFragment : FadeThroughFragment(R.layout.fragment_quotation) {

    private lateinit var binding: FragmentQuotationBinding
    private val viewModel: QuotationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View {
        binding = FragmentQuotationBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var fromUser = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observers
        viewModel.client.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.clientInput.text.toString() != text) {
                binding.clientInput.setText(text)
            }
        }

        viewModel.eventName.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.eventNameInput.text.toString() != text) {
               binding.eventNameInput.setText(text)
            }
        }
        viewModel.eventLocation.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.eventLocationInput.text.toString() != text) {
               binding.eventLocationInput.setText(text)
            }
        }

        viewModel.eventDate.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.eventDateInput.text.toString() != text) {
                binding.eventDateInput.setText(text)
            }
        }
        viewModel.eventTime.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.timeInput.text.toString() != text) {
               binding.timeInput.setText(text)
            }
        }
        viewModel.amountCharged.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.amountInput.text.toString() != text) {
                binding.amountInput.setText(text)
            }
        }


        // Text fields
        binding.clientInput.doAfterTextChanged { editable ->
            if (fromUser) {
                viewModel.client.value = editable.toString()
            } 
            fromUser = true // Always reset to true after a change
        }

        binding.eventNameInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.eventName.value = it.toString()
            }
            fromUser = true
        }

        binding.eventLocationInput.doAfterTextChanged {
            if(fromUser) {
               viewModel.eventLocation.value = it.toString()
            }
            fromUser = true
        }

        binding.eventDateInput.setOnClickListener {
            // Show date picker and update viewModel.eventDate
            showDatePicker()
        }

        binding.timeInput.setOnClickListener {
            // Show time picker and update viewModel.eventTime
            showTimePicker()
        }

        binding.amountInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.amountCharged.value = it.toString()
            }
            fromUser = true
        }

        // Chips
        //Job Type
        binding.jobTypeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.chipComedySet -> "Comedy Set"
                R.id.chipMC -> "MC"
                else -> ""
            }
            viewModel.jobType.value = type
        }

        // Job Duration
        binding.jobDurationChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val duration = when (checkedId) {
                R.id.chip30min -> "30 Minutes"
                R.id.chip1hour -> "1 Hour"
                R.id.chip2hours -> "2 Hours"
                R.id.chipWholeDay -> "Whole Day"
                else -> ""
            }
            viewModel.jobDuration.value = duration
        }


        // Generate button
        binding.btnGenerate.setOnClickListener {
            lifecycleScope.launch {
                if (validateFields()) {
                    capitalizeFields()
                    // Proceed with PDF generation or other actions
                    try {
                        val success = viewModel.generateQuotationPDF(requireContext())

                        if (success) {
                            viewModel.saveRecordToDatabase()
                            clearFields()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        // Clear button
        binding.btnClear.setOnClickListener {
           clearFields()
        }
    }


    private fun clearFields() {

        viewModel.client.value = ""
        viewModel.eventName.value = ""
        viewModel.eventLocation.value = ""
        viewModel.eventDate.value = ""
        viewModel.eventTime.value = ""
        viewModel.amountCharged.value = ""
        viewModel.jobType.value = ""
        viewModel.jobDuration.value = ""


        binding.jobTypeChipGroup.clearCheck()
        binding.jobDurationChipGroup.clearCheck()

    }


    private fun showDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder().apply {
            // Disallow past dates
            setValidator(DateValidatorPointForward.now())
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Event Date")
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.show(parentFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(selectedDate))
            binding.eventDateInput.setText(formattedDate)
            viewModel.eventDate.value = formattedDate
            binding.eventDateInput.error = null // Clear error when date is selected
            viewModel.eventDate.value = formattedDate

        }
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Select Event Time")
            .setHour(18)
            .setMinute(0)
            .build()

        timePicker.show(parentFragmentManager, "TIME_PICKER")

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val formattedTime = String.format("%02dh%02d", hour, minute)
            binding.timeInput.setText(formattedTime)
            viewModel.eventTime.value = formattedTime
            binding.timeInput.error = null // Clear error when time is selected
            viewModel.eventTime.value = formattedTime

        }
    }

    private fun validateChipGroup(chipGroup: ChipGroup, fieldName: String): Boolean {
        return if (chipGroup.checkedChipId == View.NO_ID) {
            Toast.makeText(requireContext(), "$fieldName is required", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }


    private fun capitalizeFields() {
        viewModel.client.value = viewModel.client.value?.capitalizeWords()
        viewModel.eventName.value = viewModel.eventName.value?.capitalizeWords()
        viewModel.eventLocation.value = viewModel.eventLocation.value?.capitalizeWords()
    }


    private fun validateFields(): Boolean {
        var isValid = true

        if (viewModel.client.value.isNullOrBlank()) {
            binding.clientInput.error = "Client name is required"
            isValid = false
        }

        if (viewModel.eventName.value.isNullOrBlank()) {
            binding.eventNameInput.error = "Event name is required"
            isValid = false
        }

        if (viewModel.eventLocation.value.isNullOrBlank()) {
            binding.eventLocationInput.error = "Event location is required"
            isValid = false
        }

        if (viewModel.eventDate.value.isNullOrBlank()) {
            binding.eventDateInput.error = "Event date is required"
            isValid = false
        }

        if (viewModel.eventTime.value.isNullOrBlank()) {
            binding.timeInput.error = "Event time is required"
            isValid = false
        }

        if (viewModel.amountCharged.value.isNullOrBlank()) {
            binding.amountInput.error = "Amount charged is required"
            isValid = false
        }

        if (!validateChipGroup(binding.jobTypeChipGroup, "Job Type")) {
            isValid = false
        }

        if (!validateChipGroup(binding.jobDurationChipGroup, "Job Duration")) {
            isValid = false
        }

        return isValid
    }

}