package com.zebra.sample.multiactivitysample1.ui.first

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebra.sample.multiactivitysample1.R
import com.zebra.sample.multiactivitysample1.databinding.ActivityMainBinding
import com.zebra.sample.multiactivitysample1.scanner.DWCommunicationWrapper
import com.zebra.sample.multiactivitysample1.ui.adapter.ItemAdapter
import com.zebra.sample.multiactivitysample1.ui.second.SecondActivity

// Main activity that handles UI initialization, observes ViewModel state, and interacts with DataWedge.
class MainActivity : AppCompatActivity() {

    // Binding for accessing views in the activity layout.
    private lateinit var binding: ActivityMainBinding

    // ViewModel instance scoped to the activity lifecycle.
    private val viewModel by viewModels<MainViewModel>()

    // Flags to track profile creation and initial configuration progression.
    private var isProfileCreated = false
    private var initialConfigInProgression = false

    // Adapter for managing RecyclerView items.
    private val itemAdapter = ItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mark initial configuration as in progress.
        initialConfigInProgression = true

        // Initialize the view components and layout.
        initView()

        // Register observers to listen for changes in ViewModel state.
        registerObservers()

        // Register broadcast receivers for DataWedge communication.
        DWCommunicationWrapper.registerReceivers()

        // Query DataWedge status to initialize the profile and settings.
        viewModel.getStatus()
    }

    // Sets up LiveData observers to update the UI based on ViewModel changes.
    private fun registerObservers() {
        viewModel.isLoading.observe(this) {
            // Show or hide loading indicator based on loading state.
            binding.clLoading.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.scanViewStatus.observe(this) { scanViewState ->
            // Handle profile creation status.
            scanViewState.dwProfileCreate?.let { dwProfileCreate ->
                if (dwProfileCreate.isProfileCreated) {
                    viewModel.setConfig()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.profile_creation_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // Handle profile update status.
            scanViewState.dwProfileUpdate?.let { dwProfileUpdate ->
                if (dwProfileUpdate.isProfileUpdated) {
                    initialConfigInProgression = false
                }
            }

            // Handle DataWedge status and create profile if necessary.
            scanViewState.dwStatus?.let { dwStatus ->
                if (dwStatus.isEnable) {
                    if (!isProfileCreated) {
                        viewModel.createProfile()
                        isProfileCreated = true
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.datawedge_is, dwStatus.statusString),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // Add scanned output data to the RecyclerView and scroll to the top.
            scanViewState.dwOutputData?.let { dwOutputData ->
                itemAdapter.addItem(dwOutputData)
                binding.rvActivity1.smoothScrollToPosition(0)
            }

            // Update scanner state information in the UI.
            scanViewState.dwScannerState?.let { dwScannerState ->
                binding.tvProfile.text = dwScannerState.profileName
                binding.tvScannerStatus.text = dwScannerState.statusStr
            }
        }
    }

    // Initializes the activity view components and sets up UI elements.
    private fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        enableEdgeToEdge()
        setContentView(binding.root)

        // Set the action bar title.
        supportActionBar?.title = getString(R.string.activity_1)

        // Apply window insets to adjust layout for system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView with adapter and layout manager.
        with(binding.rvActivity1) {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        // Launch SecondActivity when the button is clicked.
        binding.buttonFirst.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receivers and notifications.
        DWCommunicationWrapper.unregisterReceivers()
        viewModel.unregisterNotifications()
    }

    override fun onResume() {
        super.onResume()
        // Set configuration if initial setup is complete.
        if (!initialConfigInProgression) viewModel.setConfig()
    }
}
