package com.zebra.sample.multifragmentsample1.ui.second

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zebra.sample.multifragmentsample1.data.ScanStateHolder
import com.zebra.sample.multifragmentsample1.data.models.DWScanner
import com.zebra.sample.multifragmentsample1.scanner.ConfigurationManager
import com.zebra.sample.multifragmentsample1.scanner.QueryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SecondViewModel : ViewModel() {

    private val configurationManager = ConfigurationManager()

    private val queryManager = QueryManager()

    val scanViewStatus = ScanStateHolder.scanViewStatus

    // LiveData for storing the list of available scanners.
    val dwScanners: MutableLiveData<List<DWScanner>> = MutableLiveData(
        listOf(DWScanner("Loading...", false, "auto", ""))
    )

    val selectedPosition = MutableLiveData<Int>(0)

    var prevSelectedPosition = 0

    val isScanEnable: MutableLiveData<Boolean> = MutableLiveData()

    val isPicklistEnable: MutableLiveData<Boolean> = MutableLiveData()

    val isDecodeScreen: MutableLiveData<Boolean> = MutableLiveData()

    // Handles changes to the scanner enable/disable state from the UI switch.
    fun onCheckChanged(isChecked: Boolean) {
        isScanEnable.value = isChecked
        toggleScanner(isChecked)
        if (!isChecked){
            isPicklistEnable.value=false
            isDecodeScreen.value=false
        }
    }

    // Toggles the scanner plugin state in DataWedge (enable/disable).
    fun toggleScanner(isEnable: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.toggleScannerPlugin(isEnable)
        }
    }

    // Sets scanner configuration parameters like picklist and decode screen notification.
    fun setConfig() {
        val scanParams = bundleOf(
            "picklist" to if (isPicklistEnable.value == true) "1" else "0",
            "decode_screen_notification" to (isDecodeScreen.value == true).toString()
        )
        configurationManager.switchScanParams(scanParams)
    }

    // Initiates a soft scan using DataWedge.
    fun onSoftScan() {
        viewModelScope.launch(Dispatchers.IO) {
            configurationManager.softScanToggle()
        }
    }

    // Enumerates available scanners by querying DataWedge.
    fun enumerateScanners() {
        queryManager.enumerateScanners()
    }

    // Handles scanner selection changes from the UI dropdown or list.
    fun onScannerSelect(position: Int) {
        if (position == prevSelectedPosition) return // Ignore selection if the same scanner is reselected.
        dwScanners.value?.get(position)?.index?.let {
            configurationManager.switchScanner(it)
        }
    }
}
