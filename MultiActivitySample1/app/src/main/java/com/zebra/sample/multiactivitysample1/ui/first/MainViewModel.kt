package com.zebra.sample.multiactivitysample1.ui.first

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.PROFILE_SWITCH
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.SCANNER_STATUS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.DW_POLLING_DELAY
import com.zebra.sample.multiactivitysample1.data.ScanStateHolder
import com.zebra.sample.multiactivitysample1.data.models.ActivityName
import com.zebra.sample.multiactivitysample1.scanner.ConfigurationManager
import com.zebra.sample.multiactivitysample1.scanner.QueryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel for MainActivity, responsible for managing DataWedge configurations and status checks.
class MainViewModel : ViewModel() {

    private val queryManager = QueryManager()
    private val configurationManager = ConfigurationManager()

    // LiveData to track the loading state.
    val isLoading = ScanStateHolder.isLoading

    // LiveData to observe the scan view status.
    val scanViewStatus = ScanStateHolder.scanViewStatus

    // Initiates a loop to check DataWedge status until it is ready.
    fun getStatus() {
        isLoading.value = ScanStateHolder.isDataWedgeReady.value == false
        viewModelScope.launch(Dispatchers.IO) {
            while (ScanStateHolder.isDataWedgeReady.value == false) {
                queryManager.getDataWedgeStatus() // Request DataWedge status.
                delay(DW_POLLING_DELAY) // Delay between status checks.
            }
        }
    }

    // Creates a DataWedge profile for the first activity.
    fun createProfile() {
        isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                configurationManager.createProfile(ActivityName.FIRST)
                configurationManager.createProfile(ActivityName.SECOND)
            }
        }
    }

    // Sets the configuration for the DataWedge profile and registers for notifications.
    fun setConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.updateProfile1() // Update profile settings.
            configurationManager.registerForNotifications(
                arrayListOf(
                    PROFILE_SWITCH, // Notification for profile switching.
                    SCANNER_STATUS  // Notification for scanner status changes.
                )
            )
        }
    }

    // Unregisters from DataWedge notifications.
    fun unregisterNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.unregisterForNotifications(
                arrayListOf(
                    PROFILE_SWITCH, // Notification for profile switching.
                    SCANNER_STATUS  // Notification for scanner status changes.
                )
            )
        }
    }

    // Toggles the soft scan feature using DataWedge.
    fun onSoftScan() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.softScanToggle()
        }
    }
}