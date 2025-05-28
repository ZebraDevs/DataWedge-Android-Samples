package com.zebra.sample.multiactivitysample1.ui.second

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebra.sample.multiactivitysample1.R
import com.zebra.sample.multiactivitysample1.data.state.ScanViewState
import com.zebra.sample.multiactivitysample1.databinding.ActivitySecondBinding
import com.zebra.sample.multiactivitysample1.scanner.DWCommunicationWrapper
import com.zebra.sample.multiactivitysample1.ui.adapter.ItemAdapter

// Activity that handles UI interactions, observes ViewModel state, and interacts with DataWedge for scanner management.
class SecondActivity : AppCompatActivity() {
    // Binding for accessing views in the activity layout.
    private lateinit var binding: ActivitySecondBinding

    // ViewModel instance scoped to the activity lifecycle.
    private val viewModel by viewModels<SecondViewModel>()

    // Flag to track if profile creation has been initiated.
    private var isProfileCreateCalled = false

    // Adapter for managing RecyclerView items.
    private val itemAdapter = ItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the view components and layout.
        initViews()

        // Register observers to listen for changes in ViewModel state.
        registerObservers()

        // Register broadcast receivers for DataWedge communication.
        DWCommunicationWrapper.registerReceivers()

        // Query DataWedge status to initialize the profile and settings.
        viewModel.getStatus()
    }

    // Sets up LiveData observers to update the UI based on ViewModel changes.
    private fun registerObservers() {
        viewModel.scanViewStatus.observe(this, observerScanViewStatus)

        viewModel.isLoading.observe(this) {
            // Show or hide loading indicator based on loading state.
            binding.clLoading.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    // Initializes the activity view components and sets up UI elements.
    private fun initViews() {
        binding = ActivitySecondBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        enableEdgeToEdge()
        setContentView(binding.root)

        // Set the action bar title.
        supportActionBar?.title = getString(R.string.activity_2)

        // Apply window insets to adjust layout for system bars.
        ViewCompat.setOnApplyWindowInsetsListener(binding.second) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView with adapter and layout manager.
        with(binding.rvActivity2) {
            adapter = itemAdapter
            layoutManager =
                LinearLayoutManager(this@SecondActivity, LinearLayoutManager.VERTICAL, false)
        }

        // Toggle scanner state when the switch is checked/unchecked.
        binding.swScanner.setOnCheckedChangeListener { _, checked ->
            viewModel.onCheckChanged(checked)
        }

        // Handle back button press to return to the previous activity.
        binding.buttonSecond.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Observer to handle scan view status updates from the ViewModel.
    private val observerScanViewStatus = Observer<ScanViewState> { scanViewState ->
        // Handle profile creation status.
        scanViewState.dwProfileCreate?.let { dwProfileCreate ->
            if (dwProfileCreate.isProfileCreated) {
                viewModel.toggleScanner(viewModel.isScanEnable.value == true)
            } else {
                Toast.makeText(
                    this@SecondActivity,
                    getString(R.string.profile_creation_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Handle profile update status.
        scanViewState.dwProfileUpdate?.let { dwProfileUpdate ->
            if (dwProfileUpdate.isProfileUpdated) {
                viewModel.toggleScanner(viewModel.isScanEnable.value == true)
            }
        }

        // Handle profile switch status and creation if necessary.
        scanViewState.dwProfileSwitch?.let { dwProfileSwitch ->
            if (!dwProfileSwitch.isSwitchSuccess) {
                if (!isProfileCreateCalled) {
                    viewModel.createProfile()
                    isProfileCreateCalled = true
                }
            } else {
                viewModel.toggleScanner(viewModel.isScanEnable.value == true, true)
            }
        }

        // Handle DataWedge status and create profile if necessary.
        scanViewState.dwStatus?.let { dwStatus ->
            if (dwStatus.isEnable) {
                if (!isProfileCreateCalled) {
                    viewModel.createProfile()
                    isProfileCreateCalled = true
                }
            } else {
                Toast.makeText(
                    this@SecondActivity,
                    getString(R.string.datawedge_is, dwStatus.statusString),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Add scanned output data to the RecyclerView and scroll to the top.
        scanViewState.dwOutputData?.let { dwOutputData ->
            itemAdapter.addItem(dwOutputData)
            binding.rvActivity2.smoothScrollToPosition(0)
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

        // Handle scanner switching and update scan parameters.
        scanViewState.dwScannerSwitch?.let { dwScannerSwitch ->
            if (dwScannerSwitch.isSuccess) {
                viewModel.prevSelectedPosition = viewModel.selectedPosition.value ?: 0
                viewModel.setScanParams()
            } else {
                viewModel.selectedPosition.value = viewModel.prevSelectedPosition
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Toggle scanner state and enumerate available scanners.
        viewModel.toggleScanner(viewModel.isScanEnable.value == true)
        viewModel.enumerateScanners()
    }
}