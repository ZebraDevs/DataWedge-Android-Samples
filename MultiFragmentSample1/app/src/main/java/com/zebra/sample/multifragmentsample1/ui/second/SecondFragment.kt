package com.zebra.sample.multifragmentsample1.ui.second

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebra.sample.multifragmentsample1.R
import com.zebra.sample.multifragmentsample1.data.ScanStateHolder
import com.zebra.sample.multifragmentsample1.data.state.ScanViewState
import com.zebra.sample.multifragmentsample1.databinding.FragmentSecondBinding
import com.zebra.sample.multifragmentsample1.ui.adapter.ItemAdapter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    // Binding object for accessing views in the fragment layout.
    // Valid only between onCreateView and onDestroyView.
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance scoped to the fragment lifecycle.
    private val viewModel by viewModels<SecondViewModel>()

    // Adapter for managing the RecyclerView items.
    private val itemAdapter = ItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout and bind the ViewModel and lifecycle owner.
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RecyclerView and other UI components.
        initiateView()

        // Register observers to listen for changes in ViewModel state.
        registerObservers()
    }

    private fun initiateView() {
        with(binding.rvFragment2) {
            adapter = itemAdapter // Attach the adapter to the RecyclerView.
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        // Navigate back to the first fragment when the button is clicked.
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        // Toggle scanner state when the switch is checked/unchecked.
        binding.swScanner.setOnCheckedChangeListener { _, checked ->
            viewModel.onCheckChanged(checked)
        }
    }

    // Registers observers for ViewModel LiveData to update the UI dynamically.
    private fun registerObservers() {
        viewModel.scanViewStatus.observe(viewLifecycleOwner) { scanViewState ->
            // Add scanned output data to the RecyclerView.
            scanViewState.dwOutputData?.let { dwOutputData ->
                itemAdapter.addItem(dwOutputData)
                binding.rvFragment2.smoothScrollToPosition(0) // Scroll to the top.
            }

            // Update scanner state information in the UI.
            scanViewState.dwScannerState?.let { dwScannerState ->
                binding.tvProfile2.text = dwScannerState.profileName
                binding.tvScannerStatus2.text = dwScannerState.statusStr
            }

            // Update the list of scanners in the ViewModel.
            scanViewState.swScannerList?.let { dwScanners ->
                viewModel.dwScanners.value = dwScanners
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Toggle scanner state according to UI and enumerate available scanners.
        viewModel.toggleScanner(viewModel.isScanEnable.value == true)
        viewModel.enumerateScanners()
    }

    override fun onPause() {
        super.onPause()
        // Reset scan view state and ensure the scanner is enabled.
        ScanStateHolder.scanViewStatus.value = ScanViewState()
        viewModel.toggleScanner(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding to prevent memory leaks.
        _binding = null
    }
}
