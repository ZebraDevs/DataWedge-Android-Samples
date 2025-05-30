package com.zebra.sample.multifragmentsample1.ui.first

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebra.sample.multifragmentsample1.R
import com.zebra.sample.multifragmentsample1.data.state.ScanViewState
import com.zebra.sample.multifragmentsample1.databinding.FragmentFirstBinding
import com.zebra.sample.multifragmentsample1.ui.adapter.ItemAdapter

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    // Binding object for accessing views in the fragment layout.
    // Valid only between onCreateView and onDestroyView.
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance scoped to the fragment lifecycle.
    private val viewModel by viewModels<FirstViewModel>()

    // Adapter for managing the RecyclerView items.
    private val itemAdapter = ItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout and bind the ViewModel.
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        _binding?.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up UI components and attach adapter to RecyclerView.
        initiateView()

        // Register observers to listen for changes in ViewModel state.
        registerObservers()
    }

    // Initializes the RecyclerView and sets up click listeners for UI components.
    private fun initiateView() {
        with(binding.rvFragment1) {
            adapter = itemAdapter // Attach the adapter to the RecyclerView.
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        // Navigate to the second fragment when the button is clicked.
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_SecondFragment
            )
        }
    }

    // Registers observers for ViewModel LiveData to update the UI dynamically.
    private fun registerObservers() {
        viewModel.scanViewStatus.observe(viewLifecycleOwner) { scanViewState ->
            // Handle profile creation status.
            scanViewState.dwProfileCreate?.let { dwProfileCreate ->
                if (dwProfileCreate.isProfileCreated) {
                    viewModel.setConfig() // Configure DataWedge if the profile is created.
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Profile creation failed..!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // Handle profile update status.
            scanViewState.dwProfileUpdate?.let {
                viewModel.isInitialConfigInProgress = false
            }

            // Add scanned output data to the RecyclerView.
            scanViewState.dwOutputData?.let { dwOutputData ->
                itemAdapter.addItem(dwOutputData)
                binding.rvFragment1.smoothScrollToPosition(0) // Scroll to the top.
            }

            // Update scanner state information in the UI.
            scanViewState.dwScannerState?.let { dwScannerState ->
                binding.tvProfile.text = dwScannerState.profileName
                binding.tvScannerStatus.text = dwScannerState.statusStr
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Reset scan view state when fragment goes into the paused state.
        viewModel.scanViewStatus.value = ScanViewState()
    }

    override fun onResume() {
        super.onResume()
        // Reset configuration if the initial setup is complete.
        if (!viewModel.isInitialConfigInProgress) viewModel.resetConfig()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding to prevent memory leaks.
        _binding = null
    }
}
