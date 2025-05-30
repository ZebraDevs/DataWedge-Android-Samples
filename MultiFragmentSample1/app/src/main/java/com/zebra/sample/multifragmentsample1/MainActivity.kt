package com.zebra.sample.multifragmentsample1

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.zebra.sample.multifragmentsample1.databinding.ActivityMainBinding
import com.zebra.sample.multifragmentsample1.scanner.DWCommunicationWrapper

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // Instantiates the ViewModel scoped to this activity using the Android ViewModel library.
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initiate view components and register for view state observers
        initiateView()

        // Observes changes in scanViewStatus from the ViewModel.
        registerObservers()

        // Registers broadcast receivers to listen for DataWedge events.
        viewModel.registerReceivers()

        // Fetches the initial status from the ViewModel.
        viewModel.getStatus()

        // create DataWedge profile
        viewModel.createProfile()
    }

    private fun initiateView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun registerObservers() {
        viewModel.scanViewStatus.observe(this) {
            it.dwStatus?.let { dwStatus ->
                if (!dwStatus.isEnable) {
                    // Shows a toast message with the current DataWedge status string.
                    Toast.makeText(
                        this,
                        "DataWedge is ${dwStatus.statusString}..!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        // Observes the loading status from the ViewModel to show or hide the loading indicator.
        viewModel.isLoading.observe(this) {
            binding.clLoading.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregisters broadcast receivers and notifications to clean up resources and prevent leaks.
        DWCommunicationWrapper.unregisterReceivers()
        viewModel.unregisterNotifications()
    }
}