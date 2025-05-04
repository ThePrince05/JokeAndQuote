package com.project.jokeandquote.view

import HistoryAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.jokeandquote.R
import com.project.jokeandquote.databinding.FragmentHistoryBinding
import com.project.jokeandquote.service.PdfService
import com.project.jokeandquote.viewmodel.HistoryViewModel
import com.project.jokeandquote.viewmodel.HistoryViewModelFactory

class HistoryFragment : FadeThroughFragment(R.layout.fragment_history) {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var viewModel: HistoryViewModel



      override fun onCreateView(
          inflater: LayoutInflater, container: ViewGroup?,
          savedInstanceState: Bundle?
          ): View? {
            binding = FragmentHistoryBinding.inflate(inflater, container, false)

          val pdfService = PdfService(requireContext().applicationContext)
          val factory = HistoryViewModelFactory(requireActivity().application, pdfService)
          viewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]

          // 1. Setup RecyclerView with adapter
          val adapter = HistoryAdapter()
          binding.recordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
          binding.recordsRecyclerView.adapter = adapter

          // 2. Observe LiveData from ViewModel
          viewModel.records.observe(viewLifecycleOwner) { historyList ->
              if (historyList.isEmpty()) {
                  binding.emptyMessage.visibility = View.VISIBLE
                  binding.recordsRecyclerView.visibility = View.GONE
              } else {
                  binding.emptyMessage.visibility = View.GONE
                  binding.recordsRecyclerView.visibility = View.VISIBLE
                  adapter.submitList(historyList)
              }
          }

          // 3. Load all records (if not already called in ViewModel init)
          viewModel.loadAllRecords()

          // Delete on hold
          adapter.onItemLongClick = { record ->
              MaterialAlertDialogBuilder(requireContext())
                  .setTitle("Delete Record")
                  .setMessage("Are you sure you want to delete this record?")
                  .setPositiveButton("Delete") { _, _ ->
                      viewModel.deleteRecord(record)
                  }
                  .setNegativeButton("Cancel", null)
                  .show()
          }

          // Setup dropdown values
          val filterOptions = listOf("All", "Quotation", "Invoice")
          val adapterDropdown = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions)
          binding.filterDropdown.setAdapter(adapterDropdown)

          // Handle selection
          binding.filterDropdown.setOnItemClickListener { _, _, position, _ ->
          val selectedFilter = filterOptions[position]
          viewModel.filterRecords(selectedFilter)
          }

            return binding.root
        }

    private var fromUser = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observers
        viewModel.searchText.observe(viewLifecycleOwner) { text ->
            fromUser = false
            if (binding.searchInput.text.toString() != text) {
                binding.searchInput.setText(text)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.recordsRecyclerView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.records.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isEmpty()) {
                binding.emptyMessage.visibility = View.VISIBLE
                binding.recordsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyMessage.visibility = View.GONE
                binding.recordsRecyclerView.visibility = View.VISIBLE
            }
            // Update the RecyclerView with new data
            (binding.recordsRecyclerView.adapter as HistoryAdapter).submitList(historyList)
        }



        // Text fields
        binding.searchInput.doAfterTextChanged { editable ->
            if (fromUser) {
                viewModel.searchText.value = editable.toString()
            }
            fromUser = true // Always reset to true after a change
        }

        // Setup Dropdown
        setupFilterDropdown()

    }

    private fun setupFilterDropdown() {
        val filterOptions = listOf("All", "Quotation", "Invoice")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions)
        binding.filterDropdown.setAdapter(adapter)
    }

}