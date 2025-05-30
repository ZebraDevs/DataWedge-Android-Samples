package com.zebra.sample.multifragmentsample1.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.zebra.sample.multifragmentsample1.data.models.DWScanner
import com.zebra.sample.multifragmentsample1.App
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.ACTION_DATAWEDGE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.ACTION_RESULT
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.ACTION_RESULT_NOTIFICATION
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_COMMAND_IDENTIFIER
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.APPLICATION_EVENT_ACTION
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_NOTIFICATIONS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_PROFILENAME
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.OUTPUT_DATA_LABEL_TYPE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.OUTPUT_DATA_STRING
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.PROFILE_SWITCH
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.RESULT_GET_DATAWEDGE_STATUS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.RESULT_RESULT_ENUMERATE_SCANNERS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.SCANNER_STATUS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SET_CONFIG
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SCAN_PARAM
import com.zebra.sample.multifragmentsample1.data.ScanStateHolder
import com.zebra.sample.multifragmentsample1.data.models.DWOutputData
import com.zebra.sample.multifragmentsample1.data.models.DWProfileCreate
import com.zebra.sample.multifragmentsample1.data.models.DWProfileUpdate
import com.zebra.sample.multifragmentsample1.data.models.DWScannerState
import com.zebra.sample.multifragmentsample1.data.models.DWStatus
import com.zebra.sample.multifragmentsample1.data.state.ScanViewState
// Constant representing a successful operation result.
const val SUCCESS = "SUCCESS"

//Wrapper for handling communication with DataWedge API.
object DWCommunicationWrapper {

    // Tag for logging purposes, using the canonical name of the class.
    private val TAG = this.javaClass.canonicalName

    // Function to register broadcast receivers for listening to DataWedge-related intents.
    fun registerReceivers() {
        Log.d(TAG, "registerReceivers()")

        val filter = IntentFilter()
        // Adds an action to listen for DataWedge notification results.
        filter.addAction(ACTION_RESULT_NOTIFICATION)
        filter.addAction(ACTION_RESULT)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addAction(ACTION_DATAWEDGE)
        // Adds an action to listen for application-level events triggered by DataWedge.
        filter.addAction(APPLICATION_EVENT_ACTION)

        // Registers the broadcast receiver with the specified intent filter.
        ContextCompat.registerReceiver(
            App.getInstance(),
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    // Function to unregister broadcast receivers to stop listening for DataWedge-related intents.
    fun unregisterReceivers() {
        App.getInstance().unregisterReceiver(broadcastReceiver)
        dwBroadcastReceiver
    }

    private val dwBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, i: Intent?) {
            i?.let { intent->
                when {
                    intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_DATAWEDGE_STATUS") -> {
                        // Handles the result of querying DataWedge status.
                        val dwStatus = intent.
                        getStringExtra("com.symbol.datawedge.api.RESULT_GET_DATAWEDGE_STATUS")

                        val isEnable= dwStatus == "ENABLED"

                        ScanStateHolder.scanViewStatus.value = ScanViewState(
                            dwStatus = DWStatus(
                                isEnable,
                                dwStatus ?: ""
                            )
                        )
                        ScanStateHolder.isDataWedgeReady.value = isEnable
                    }
                }
            }
        }
    }

    // Broadcast receiver for handling incoming intents from DataWedge.
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                // Handles the intent based on the action or extras it contains.
                when {
                    it.hasExtra(RESULT_GET_DATAWEDGE_STATUS) -> {
                        // Handles the result of querying DataWedge status.
                        handleDataWedgeStatusResult(it)
                    }
                    it.action.equals(APPLICATION_EVENT_ACTION) -> {
                        // Handles application-level events triggered by DataWedge.
                        handleDataWedgeIntentOutput(it)
                    }
                    it.hasExtra(RESULT_RESULT_ENUMERATE_SCANNERS) -> {
                        // Handles enumeration of scanners.
                        handleScannerEnumeration(intent)
                    }
                    it.hasExtra(EXTRA_COMMAND_IDENTIFIER) -> {
                        // Handles actions with specific command identifiers.
                        handleIntentActionsWithResults(intent)
                    }
                    it.hasExtra(EXTRA_NOTIFICATIONS) -> {
                        // Handles notifications received from DataWedge.
                        handleDataWedgeNotifications(intent)
                    }
                    else -> {
                        // Logs intents that are not considered for processing.
                        Log.d(TAG, "Not considered: $intent")
                    }
                }
            }
        }
    }

    // Function to handle notifications received from DataWedge.
    private fun handleDataWedgeNotifications(intent: Intent) {
        intent.getBundleExtra("com.symbol.datawedge.api.NOTIFICATION")?.let { data ->
            val notificationType = data.getString("NOTIFICATION_TYPE")
            when (notificationType) {
                SCANNER_STATUS -> {
                    // Handles scanner status notifications.
                    val scannerStatus = data.getString("STATUS") ?: ""
                    val profileName = data.getString("PROFILE_NAME") ?: ""
                    if (scannerStatus == "IDLE " && profileName == EXTRA_PROFILENAME) {
                        ScanStateHolder.isScannerIdle.value = true
                    } else if (scannerStatus == "DISABLED" && profileName == EXTRA_PROFILENAME) {
                        ScanStateHolder.isScannerIdle.value = false
                    }
                    ScanStateHolder.scanViewStatus.value = ScanViewState(
                        dwScannerState = DWScannerState(profileName, scannerStatus)
                    )
                }
                PROFILE_SWITCH -> {
                    // Updates the profile name when a profile switch notification is received.
                    ScanStateHolder.profileName.value = data.getString("PROFILE_NAME")
                }
            }
        }
    }

    // Function to handle intents with specific command results.
    private fun handleIntentActionsWithResults(intent: Intent) {
        val identifier = intent.getStringExtra(EXTRA_COMMAND_IDENTIFIER)
        Log.d(TAG, identifier.toString())
        intent.getStringExtra("RESULT")?.let { result ->
            val resultCode = intent.getBundleExtra("RESULT_INFO")?.getString("RESULT_CODE")
            val msg = when (identifier) {
                //Handle switch scanner param update results
                VALUE_COMMAND_IDENTIFIER_SCAN_PARAM -> {
                    if (result == SUCCESS) "Scanner param changed..!"
                    else {
                        val errorCode = intent.getBundleExtra("RESULT_INFO")?.getStringArray("RESULT_CODE")?.get(0)
                        "Switch scanner param failed -> $errorCode"
                    }
                }
                //Handle create profile API results
                VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE -> {
                    if (result == SUCCESS) {
                        ScanStateHolder.scanViewStatus.value =
                            ScanViewState(dwProfileCreate = DWProfileCreate(true))
                        "Profile created..!"
                    } else {
                        if (resultCode == "PROFILE_ALREADY_EXISTS") {
                            ScanStateHolder.scanViewStatus.value =
                                ScanViewState(dwProfileCreate = DWProfileCreate(true))
                            "Profile already exists..!"
                        } else {
                            "Profile creation failed..!"
                        }
                    }
                }
                //Handle set config API results
                VALUE_COMMAND_IDENTIFIER_SET_CONFIG -> {
                    if (result == SUCCESS) {
                        ScanStateHolder.isLoading.value = false
                        ScanStateHolder.scanViewStatus.value =
                            ScanViewState(dwProfileUpdate = DWProfileUpdate(true))
                        "Profile updated..!"
                    } else {
                        "Set config failed with -> $resultCode"
                    }
                }
                //Handle other ->
                else -> {
                    if (result == SUCCESS)
                        "Successfully result received..!"
                    else {
                        Log.d(TAG, "$result -> $identifier -> $resultCode")
                        "Something went wrong..!"
                    }
                }
            }

            // Displays a Toast message with the result and logs it.
            Toast.makeText(
                App.getInstance(),
                msg,
                Toast.LENGTH_SHORT
            ).show()
            Log.d(TAG, "RESULT->$result, $msg")
        }
    }

    // Function to handle scanner enumeration results.
    private fun handleScannerEnumeration(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(RESULT_RESULT_ENUMERATE_SCANNERS, Bundle::class.java)
        } else {
            intent.getParcelableArrayListExtra<Bundle>(RESULT_RESULT_ENUMERATE_SCANNERS)
        }?.let { list ->
            val scanList = arrayListOf<DWScanner>(
                DWScanner(name = "Auto", connectionState = false, index = "auto", identifier = "AUTO")
            )
            list.forEach { bundle ->
                scanList.add(
                    DWScanner(
                        name = bundle.getString("SCANNER_NAME") ?: "",
                        connectionState = bundle.getBoolean("SCANNER_CONNECTION_STATE"),
                        index = bundle.getInt("SCANNER_INDEX").toString(),
                        identifier = bundle.getString("SCANNER_IDENTIFIER") ?: "",
                    )
                )
            }
            ScanStateHolder.scanViewStatus.value = ScanViewState(swScannerList = scanList)
        }
    }

    // Function to handle scanned output data from DataWedge intents.
    private fun handleDataWedgeIntentOutput(it: Intent): Int {
        val data = it.getStringExtra(OUTPUT_DATA_STRING) ?: ""
        val label = it.getStringExtra(OUTPUT_DATA_LABEL_TYPE) ?: ""
        ScanStateHolder.scanViewStatus.value = ScanViewState(
            dwOutputData = DWOutputData(data, label)
        )
        return Log.w(TAG, "$data $label")
    }

    // Function to handle DataWedge status result intents.
    private fun handleDataWedgeStatusResult(it: Intent) {
        val dwStatus = it.getStringExtra(RESULT_GET_DATAWEDGE_STATUS)
        Log.d(TAG, "DataWedge status: $dwStatus")
        ScanStateHolder.scanViewStatus.value = ScanViewState(
            dwStatus = DWStatus(dwStatus == "ENABLED", dwStatus ?: "")
        )
        ScanStateHolder.isDataWedgeReady.value = true
    }
}
