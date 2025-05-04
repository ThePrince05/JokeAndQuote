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
        viewModel.client.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.clientInput.text.toString() != text) {
                binding.clientInput.setText(text)
            }
        }
        // Clear button
        binding.btnClear.setOnClickListener {
            clearFields()
        }

        // Text fields
        binding.clientInput.doAfterTextChanged { editable ->
            if (fromUser) {
                viewModel.client.value = editable.toString()
            }
            fromUser = true // Always reset to true after a change
        }
    }

}