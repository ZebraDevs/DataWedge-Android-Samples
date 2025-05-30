package com.zebra.sample.multifragmentsample1.ui.first

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.PROFILE_SWITCH
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.SCANNER_STATUS
import com.zebra.sample.multifragmentsample1.data.ScanStateHolder
import com.zebra.sample.multifragmentsample1.scanner.ConfigurationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirstViewModel : ViewModel() {

    private val configurationManager = ConfigurationManager()

    // LiveData to observe the scan view status shared via ScanStateHolder.
    val scanViewStatus = ScanStateHolder.scanViewStatus

    // Tracks whether the initial configuration process is in progress, synced with ScanStateHolder.
    var isInitialConfigInProgress: Boolean
        get() = ScanStateHolder.initialConfigInProgression
        set(value) {
            ScanStateHolder.initialConfigInProgression = value
        }

    // Toggles the soft scan trigger.
    fun onSoftScan() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.softScanToggle()
        }
    }

    // Configures the DataWedge profile and registers for notifications.
    fun setConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.updateProfile() // Updates the DataWedge profile.
            configurationManager.registerForNotifications(
                arrayListOf(
                    PROFILE_SWITCH,
                    SCANNER_STATUS
                )
            )
        }
    }

    // Resets the configuration by updating the DataWedge profile.
    fun resetConfig() {
        viewModelScope.launch {
            configurationManager.updateProfile()
        }
    }
}
