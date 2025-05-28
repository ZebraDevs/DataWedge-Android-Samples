package com.zebra.sample.multiactivitysample1.ui.second

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_PROFILE_NAME_2
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.DW_POLLING_DELAY
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.SWITCH_PARAM_WAIT_DELAY
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.TOGGLE_SCANNER_WAIT_DELAY
import com.zebra.sample.multiactivitysample1.data.ScanStateHolder
import com.zebra.sample.multiactivitysample1.data.models.ActivityName
import com.zebra.sample.multiactivitysample1.data.models.DWScanner
import com.zebra.sample.multiactivitysample1.scanner.ConfigurationManager
import com.zebra.sample.multiactivitysample1.scanner.QueryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel for SecondActivity, responsible for managing scanner configurations and state with DataWedge.
class SecondViewModel : ViewModel() {

    private val queryManager = QueryManager()
    private val configurationManager = ConfigurationManager()

    // LiveData to track the states of various scanner settings.
    val isScanEnable: MutableLiveData<Boolean> = MutableLiveData()
    val isPicklistEnable: MutableLiveData<Boolean> = MutableLiveData()
    val isDecodeScreen: MutableLiveData<Boolean> = MutableLiveData()

    // LiveData for storing the list of available scanners.
    val dwScanners: MutableLiveData<List<DWScanner>> = MutableLiveData(
        listOf(DWScanner("Loading...", false, 0, ""))
    )

    val selectedPosition = MutableLiveData<Int>(0)
    var prevSelectedPosition = 0
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

    // Creates a DataWedge profile for the second activity.
    fun createProfile() {
        isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                configurationManager.createProfile(ActivityName.SECOND)
            }
        }
    }

    // Toggles the soft scan feature using DataWedge.
    fun onSoftScan() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.softScanToggle()
        }
    }

    // Handles changes to the scanner enable/disable state from the UI switch.
    fun onCheckChanged(isChecked: Boolean) {
        isScanEnable.value = isChecked
        toggleScanner(isChecked) // Toggles the scanner state based on the checked value.
    }

    // Switches to a MultiActivityProfile_2 DataWedge profile.
    private fun switchToProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.switchToProfile(EXTRA_PROFILE_NAME_2)
        }
    }

    // Toggles the scanner plugin state in DataWedge (enable/disable).
    fun toggleScanner(isEnable: Boolean = false, isSwitch: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if the profile switch is needed and perform it.
            if (ScanStateHolder.profileName.value != EXTRA_PROFILE_NAME_2 && !isSwitch) {
                switchToProfile()
                return@launch
            }
            // Wait until the profile switch happens.
            while (ScanStateHolder.profileName.value != EXTRA_PROFILE_NAME_2 || ScanStateHolder.isScannerIdle.value==isEnable) {
                delay(TOGGLE_SCANNER_WAIT_DELAY)
            }
            configurationManager.toggleScannerPlugin(isEnable)
            // Set scan parameters if the scanner is enabled.
            if (isEnable) setScanParams()
        }
    }

    // Sets the scan parameters such as picklist and decode screen notification.
    fun setScanParams() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(100) // Added delay to ensure updated values are applied.
            // Wait for the scanner to be idle before setting parameters.
            while (ScanStateHolder.isScannerIdle.value == false) {
                delay(SWITCH_PARAM_WAIT_DELAY)
            }
            val scanParams = bundleOf(
                "picklist" to if (isPicklistEnable.value == true) "1" else "0",
                "decode_screen_notification" to (isDecodeScreen.value == true).toString()
            )
            configurationManager.switchScanParams(scanParams)
        }
    }

    // Requests the enumeration of available scanners from DataWedge.
    fun enumerateScanners() {
        queryManager.enumerateScanners()
    }

    // Handles scanner selection changes from the UI dropdown or list.
    fun onScannerSelect(position: Int) {
        if (position == prevSelectedPosition) return // Ignore selection if the same scanner is reselected.
        dwScanners.value?.get(position)?.identifier?.let {
            ScanStateHolder.isLoading.value = true
            configurationManager.switchScanner(it)
        }
    }
}