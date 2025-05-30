package com.zebra.sample.multifragmentsample1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.POLLING_WAIT_DELAY
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.PROFILE_SWITCH
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.SCANNER_STATUS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.DW_STATUS_POLLING_DELAY
import com.zebra.sample.multifragmentsample1.data.ScanStateHolder
import com.zebra.sample.multifragmentsample1.scanner.ConfigurationManager
import com.zebra.sample.multifragmentsample1.scanner.DWCommunicationWrapper
import com.zebra.sample.multifragmentsample1.scanner.QueryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val queryManager = QueryManager()
    private val configurationManager = ConfigurationManager()

    val isLoading = ScanStateHolder.isLoading
    val scanViewStatus = ScanStateHolder.scanViewStatus

    fun getStatus() {
        if (ScanStateHolder.isDataWedgeReady.value == false) {
            isLoading.value = true
            ScanStateHolder.initialConfigInProgression = true
        }

        // Continuously query DataWedge status until it receive response.
        viewModelScope.launch(Dispatchers.IO) {
            while (ScanStateHolder.isDataWedgeReady.value == false) {
                queryManager.getDataWedgeStatus()
                delay(DW_STATUS_POLLING_DELAY) // Delay to prevent rapid polling.
            }
        }
    }


    fun createProfile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                while (ScanStateHolder.isDataWedgeReady.value == false) {
                    // Wait until DataWedge is active.
                    delay(POLLING_WAIT_DELAY)
                }
                // Create the profile once DataWedge is ready.
                configurationManager.createProfile()
            }
        }
    }

    // Function to unregister notifications for DataWedge events.
    fun unregisterNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.unregisterForNotifications(
                arrayListOf(
                    PROFILE_SWITCH,
                    SCANNER_STATUS
                )
            )
        }
    }

    fun registerReceivers() {
        DWCommunicationWrapper.registerReceivers()
    }
}

