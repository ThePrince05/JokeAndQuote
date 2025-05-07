package com.project.jokeandquote.view

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.project.jokeandquote.R
import com.project.jokeandquote.databinding.FragmentQuotationBinding
import com.project.jokeandquote.databinding.FragmentSettingsBinding
import com.project.jokeandquote.viewmodel.QuotationViewModel
import com.project.jokeandquote.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.getValue

class SettingsFragment :FadeThroughFragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                // Check if the selected file is a PNG
                val mimeType = requireContext().contentResolver.getType(uri)
                if (mimeType == "image/png") {
                    // Copy the image to internal storage
                    val copiedUri = copyImageToInternalStorage(uri)
                    if (copiedUri != null) {
                        viewModel.logoUri.value = copiedUri
                        binding.logoImageView.setImageURI(copiedUri)
                    } else {
                        Toast.makeText(requireContext(), "Failed to copy image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select a PNG image with a transparent background.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    private var fromUser = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadDetailsFromDatabase()

        // Observers
        viewModel.name.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.comedianNameInput.text.toString() != text) {
                binding.comedianNameInput.setText(text)
            }
        }
        viewModel.officeAddress.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.officeAddressInput.text.toString() != text) {
                binding.officeAddressInput.setText(text)
            }
        }
        viewModel.phoneNumber.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.phoneNumberInput.text.toString() != text) {
                binding.phoneNumberInput.setText(text)
            }
        }
        viewModel.emailAddress.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.emailAddressInput.text.toString() != text) {
                binding.emailAddressInput.setText(text)
            }
        }
        viewModel.bankName.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.bankNameInput.text.toString() != text) {
                binding.bankNameInput.setText(text)
            }
        }
        viewModel.accountNumber.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.accountNumberInput.text.toString() != text) {
                binding.accountNumberInput.setText(text)
            }
        }
        viewModel.nameOnAccount.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.nameOnAccountInput.text.toString() != text) {
                binding.nameOnAccountInput.setText(text)
            }
        }


        viewModel.accountType.observe(viewLifecycleOwner) { type ->
            fromUser = false
            val chipId = when (type) {
                "Savings" -> R.id.chipSavings
                "Business" -> R.id.chipBusiness
                "Joint" -> R.id.chipJoint
                "Checking" -> R.id.chipChecking
                else -> View.NO_ID
            }
            if (chipId != View.NO_ID && binding.accountTypeChipGroup.checkedChipId != chipId) {
                binding.accountTypeChipGroup.check(chipId)
            }
            fromUser = true
        }

        viewModel.logoUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                binding.logoImageView.setImageURI(uri)
            } else {
                binding.logoImageView.setImageResource(R.drawable.ic_logo_placeholder)
            }
        }



        // Text fields
        binding.comedianNameInput.doAfterTextChanged { editable ->
            if (fromUser) {
                viewModel.name.value = editable.toString()
            }
            fromUser = true // Always reset to true after a change
        }

        binding.officeAddressInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.officeAddress.value = it.toString()
            }
            fromUser = true
        }
        binding.phoneNumberInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.phoneNumber.value = it.toString()
            }
            fromUser = true
        }

        binding.emailAddressInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.emailAddress.value = it.toString()
            }
            fromUser = true
        }

        binding.bankNameInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.bankName.value = it.toString()
            }
            fromUser = true
        }

        binding.accountNumberInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.accountNumber.value = it.toString()
            }
            fromUser = true
        }

        binding.nameOnAccountInput.doAfterTextChanged {
            if(fromUser) {
                viewModel.nameOnAccount.value = it.toString()
            }
            fromUser = true
        }

        // Type of Account Chip
        binding.accountTypeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.chipSavings -> "Savings"
                R.id.chipBusiness -> "Business"
                R.id.chipJoint -> "Joint"
                R.id.chipChecking -> "Checking"
                else -> ""
            }
            viewModel.accountType.value = type
        }


        // Buttons
        binding.btnUploadLogo.setOnClickListener {
            pickImageLauncher.launch("image/png")
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                if (validateFields()) {
                    capitalizeFields()
                    try {
                        val saveSuccessful = viewModel.saveDetailsToDatabase()
                        if(saveSuccessful) {
                            (requireActivity() as? MainActivity)?.isSetupComplete = true
                            Toast.makeText(requireContext(), "Saved successfully!", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to save in Db: ${e.message}", Toast.LENGTH_SHORT).show()
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
        viewModel.name.value = ""
        viewModel.officeAddress.value = ""
        viewModel.phoneNumber.value = ""
        viewModel.emailAddress.value = ""
        viewModel.bankName.value = ""
        viewModel.accountNumber.value = ""
        viewModel.nameOnAccount.value = ""

        binding.accountTypeChipGroup.clearCheck()
    }
    private fun capitalizeFields() {
        viewModel.name.value = viewModel.name.value?.capitalizeWords()
        viewModel.officeAddress.value = viewModel.officeAddress.value?.capitalizeWords()
        viewModel.bankName.value = viewModel.bankName.value?.capitalizeWords()
        viewModel.nameOnAccount.value = viewModel.nameOnAccount.value?.capitalizeWords()
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }


    private fun validateFields(): Boolean {
        var isValid = true

        if (viewModel.name.value.isNullOrBlank()) {
            binding.comedianNameInput.error = "Comedian name is required"
            isValid = false
        }

        if (viewModel.officeAddress.value.isNullOrBlank()) {
            binding.officeAddressInput.error = "Office address is required"
            isValid = false
        }

        if (viewModel.phoneNumber.value.isNullOrBlank()) {
            binding.phoneNumberInput.error = "Phone number is required"
            isValid = false
        }

        if (viewModel.emailAddress.value.isNullOrBlank()) {
            binding.emailAddressInput.error = "Email address is required"
            isValid = false
        }

        if (viewModel.bankName.value.isNullOrBlank()) {
            binding.bankNameInput.error = "Bank name is required"
            isValid = false
        }

        if (viewModel.accountNumber.value.isNullOrBlank()) {
            binding.accountNumberInput.error = "Account number is required"
            isValid = false
        }

        if (viewModel.nameOnAccount.value.isNullOrBlank()) {
            binding.nameOnAccountInput.error = "Name on account is required"
            isValid = false
        }


        if (!validateChipGroup(binding.accountTypeChipGroup, "Account Type")) {
            isValid = false
        }

        if (viewModel.logoUri.value == null) {
            Toast.makeText(requireContext(), "Please upload a logo", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun validateChipGroup(chipGroup: ChipGroup, fieldName: String): Boolean {
        return if (chipGroup.checkedChipId == View.NO_ID) {
            Toast.makeText(requireContext(), "$fieldName is required", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun copyImageToInternalStorage(sourceUri: Uri): Uri? {
        val resolver = requireContext().contentResolver

        return try {
            val fileName = getFileNameFromUri(sourceUri) ?: "logo_${System.currentTimeMillis()}.png"
            val destinationFile = File(requireContext().filesDir, fileName)

            // ✅ Check if the file already exists
            if (destinationFile.exists()) {
                // File already exists, no need to copy again
                return Uri.fromFile(destinationFile)
            }

            val inputStream = resolver.openInputStream(sourceUri) ?: return null

            inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                it.getString(nameIndex)
            } else null
        }
    }


}