package com.project.jokeandquote.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.project.jokeandquote.R
import com.project.jokeandquote.databinding.FragmentQuotationBinding
import com.project.jokeandquote.databinding.FragmentSettingsBinding
import com.project.jokeandquote.viewmodel.QuotationViewModel
import com.project.jokeandquote.viewmodel.SettingsViewModel
import kotlin.getValue

class SettingsFragment :FadeThroughFragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var fromUser = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observers
        viewModel.name.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.nameInput.text.toString() != text) {
                binding.nameInput.setText(text)
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


        // Text fields
        binding.nameInput.doAfterTextChanged { editable ->
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

        }
        binding.btnSave.setOnClickListener {

        }
    }

}