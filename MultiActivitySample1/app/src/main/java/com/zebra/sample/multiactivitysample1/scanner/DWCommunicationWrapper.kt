package com.zebra.sample.multiactivitysample1.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.zebra.sample.multiactivitysample1.App
import com.zebra.sample.multiactivitysample1.R
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.ACTION_DATAWEDGE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.ACTION_RESULT
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.ACTION_RESULT_NOTIFICATION
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.APPLICATION_EVENT_ACTION
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.COMMAND_IDENTIFIER
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_NOTIFICATIONS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_PROFILE_NAME_1
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_PROFILE_NAME_2
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.OUTPUT_DATA_LABEL_TYPE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.OUTPUT_DATA_STRING
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.PROFILE_SWITCH
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.RESULT_GET_DATAWEDGE_STATUS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.RESULT_RESULT_ENUMERATE_SCANNERS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.SCANNER_STATUS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SET_CONFIG
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SCAN_PARAM
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SWITCH_PROFILE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SWITCH_SCANNER_EX
import com.zebra.sample.multiactivitysample1.data.ScanStateHolder
import com.zebra.sample.multiactivitysample1.data.models.DWOutputData
import com.zebra.sample.multiactivitysample1.data.models.DWProfileCreate
import com.zebra.sample.multiactivitysample1.data.models.DWProfileSwitch
import com.zebra.sample.multiactivitysample1.data.models.DWProfileUpdate
import com.zebra.sample.multiactivitysample1.data.models.DWScanner
import com.zebra.sample.multiactivitysample1.data.models.DWScannerState
import com.zebra.sample.multiactivitysample1.data.models.DWScannerSwitch
import com.zebra.sample.multiactivitysample1.data.models.DWStatus
import com.zebra.sample.multiactivitysample1.data.state.ScanViewState


// Wrapper for handling communication with DataWedge through broadcast intents.
object DWCommunicationWrapper {
    private val SUCCESS = "SUCCESS"
    private val TAG = this.javaClass.canonicalName

    // Registers broadcast receivers to listen for DataWedge-related intents.
    fun registerReceivers() {
        Log.d(TAG, "registerReceivers()")

        val filter = IntentFilter()
        // Listen for DataWedge notification and result actions.
        filter.addAction(ACTION_RESULT_NOTIFICATION)
        filter.addAction(ACTION_RESULT)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addAction(ACTION_DATAWEDGE)
        // Listen for application-level events triggered by DataWedge.
        filter.addAction(APPLICATION_EVENT_ACTION)

        // Register the receiver with the specified intent filter.
        ContextCompat.registerReceiver(
            App.getInstance(),
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    // Unregisters the broadcast receiver to stop listening for intents.
    fun unregisterReceivers() {
        App.getInstance().unregisterReceiver(broadcastReceiver)
    }

    // BroadcastReceiver to handle different types of intents from DataWedge.
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when {
                    // Handle DataWedge status data.
                    it.hasExtra(RESULT_GET_DATAWEDGE_STATUS) -> {
                        handleDataWedgeStatusReceive(it)
                    }

                    // Handle DataWedge intent output data.
                    it.action.equals(APPLICATION_EVENT_ACTION) -> {
                        handleDataWedgeIntentOutput(it)
                    }

                    // Handle enumeration of scanners.
                    it.hasExtra(RESULT_RESULT_ENUMERATE_SCANNERS) -> {
                        handleScannerEnumeration(intent)
                    }

                    // Handle requests sent with a result "true" and a result identifier.
                    it.hasExtra(COMMAND_IDENTIFIER) -> {
                        handleResultsWithCommandIdentifiers(intent)
                    }

                    // Handle DataWedge notifications.
                    it.hasExtra(EXTRA_NOTIFICATIONS) -> {
                        handleDataWedgeNotifications(intent)
                    }

                    // Log any results that are not handled.
                    else -> {
                        Log.d(TAG, "Results not handled")
                    }
                }
            }
        }
    }

    // Handles notifications received from DataWedge.
    private fun handleDataWedgeNotifications(intent: Intent) {
        intent.getBundleExtra("com.symbol.datawedge.api.NOTIFICATION")?.let { data ->
            val notificationType = data.getString("NOTIFICATION_TYPE")
            when (notificationType) {
                SCANNER_STATUS -> {
                    val scannerStatus = data.getString("STATUS") ?: ""
                    val profileName = data.getString("PROFILE_NAME") ?: ""
                    if (scannerStatus == "IDLE" && (profileName in arrayOf(
                            EXTRA_PROFILE_NAME_1,
                            EXTRA_PROFILE_NAME_2
                        ))
                    ) {
                        ScanStateHolder.isScannerIdle.value = true
                    } else if (scannerStatus == "DISABLED" && (profileName in arrayOf(
                            EXTRA_PROFILE_NAME_1,
                            EXTRA_PROFILE_NAME_2
                        ))
                    ) {
                        ScanStateHolder.isScannerIdle.value = false
                    }
                    ScanStateHolder.scanViewStatus.value = ScanViewState(
                        dwScannerState = DWScannerState(profileName, scannerStatus)
                    )
                }

                PROFILE_SWITCH -> {
                    ScanStateHolder.profileName.value = data.getString("PROFILE_NAME")
                }
            }
        }
    }

    // Handles results from DataWedge that include command identifiers.
    private fun handleResultsWithCommandIdentifiers(intent: Intent) {
        val identifier = intent.getStringExtra(COMMAND_IDENTIFIER)
        Log.d(TAG, identifier.toString())
        intent.getStringExtra("RESULT")?.let { result ->
            val resultCode = intent.getBundleExtra("RESULT_INFO")?.getString("RESULT_CODE")
            val msg = when (identifier) {
                VALUE_COMMAND_IDENTIFIER_SWITCH_SCANNER_EX -> {
                    if (result == SUCCESS) {
                        ScanStateHolder.scanViewStatus.value =
                            ScanViewState(dwScannerSwitch = DWScannerSwitch(true))
                        ScanStateHolder.isLoading.value = false
                        "Scanner switched..!"
                    } else {
                        ScanStateHolder.isLoading.value = false
                        when (resultCode) {
                            "SCANNER_ALREADY_ENABLED" -> {
                                ScanStateHolder.scanViewStatus.value =
                                    ScanViewState(dwScannerSwitch = DWScannerSwitch(true))
                                "Scanner already enabled..!"
                            }

                            else -> {
                                ScanStateHolder.scanViewStatus.value =
                                    ScanViewState(dwScannerSwitch = DWScannerSwitch(false))
                                "Scanner switch failed..!"
                            }
                        }
                    }
                }

                VALUE_COMMAND_IDENTIFIER_SCAN_PARAM -> {
                    if (result == SUCCESS) "Scanner param changed..!"
                    else {
                        val errorCode =
                            intent.getBundleExtra("RESULT_INFO")?.getStringArray("RESULT_CODE")
                                ?.get(0)
                        "Switch scanner param failed -> $errorCode"
                    }
                }

                VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE -> {
                    if (result == SUCCESS) {
                        ScanStateHolder.scanViewStatus.value =
                            ScanViewState(dwProfileCreate = DWProfileCreate(true))
                        "Profile created..!"
                    } else {
                        if (resultCode == "PROFILE_ALREADY_EXISTS") {
                            ScanStateHolder.scanViewStatus.value =
                                ScanViewState(dwProfileCreate = DWProfileCreate(true))
                            "Profile already exist..!"
                        } else {
                            "Profile creation failed..!"
                        }
                    }
                }

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

                VALUE_COMMAND_IDENTIFIER_SWITCH_PROFILE -> {
                    if (result == SUCCESS) {
                        ScanStateHolder.scanViewStatus.value =
                            ScanViewState(dwProfileSwitch = DWProfileSwitch(true))
                        App.getInstance().getString(R.string.profile_switch_success)
                    } else {
                        val isSwitched = when (resultCode) {
                            "PROFILE_ALREADY_SET" -> true
                            else -> false
                        }
                        ScanStateHolder.scanViewStatus.value =
                            ScanViewState(
                                dwProfileSwitch = DWProfileSwitch(
                                    isSwitched,
                                    resultCode ?: ""
                                )
                            )
                        if (isSwitched)
                            App.getInstance().getString(R.string.profile_already_switched)
                        else
                            App.getInstance().getString(R.string.profile_switch_failed)
                    }
                }

                else -> {
                    if (result == SUCCESS)
                        "Successfully result received..!"
                    else {
                        Log.d(TAG, "$result -> $identifier -> $resultCode")
                        "Something went wrong..!"
                    }
                }
            }

            if (msg.isNotEmpty()) {
                Toast.makeText(
                    App.getInstance(),
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d(TAG, "RESULT->${result}, $msg")
        }
    }

    // Handles the enumeration of available scanners.
    private fun handleScannerEnumeration(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(RESULT_RESULT_ENUMERATE_SCANNERS, Bundle::class.java)
        } else {
            intent.getParcelableArrayListExtra<Bundle>(RESULT_RESULT_ENUMERATE_SCANNERS)
        }?.let { list ->
            val scanList = arrayListOf<DWScanner>(
                DWScanner("Auto", false, 0, "AUTO")
            )
            list.forEach { bunb ->
                scanList.add(
                    DWScanner(
                        bunb.getString("SCANNER_NAME") ?: "",
                        bunb.getBoolean("SCANNER_CONNECTION_STATE"),
                        bunb.getInt("SCANNER_INDEX"),
                        bunb.getString("SCANNER_IDENTIFIER") ?: "",
                    )
                )
            }
            ScanStateHolder.scanViewStatus.value = ScanViewState(swScannerList = scanList)
        }
    }

    // Handles intent outputs from DataWedge, processing scanned data.
    private fun handleDataWedgeIntentOutput(it: Intent): Int {
        val data = it.getStringExtra(OUTPUT_DATA_STRING) ?: ""
        val label = it.getStringExtra(OUTPUT_DATA_LABEL_TYPE) ?: ""
        ScanStateHolder.scanViewStatus.value = ScanViewState(
            dwOutputData = DWOutputData(data, label)
        )
        return Log.w(TAG, "$data $label")
    }

    // Handles receiving the status of DataWedge.
    private fun handleDataWedgeStatusReceive(it: Intent) {
        val dwStatus = it.getStringExtra(RESULT_GET_DATAWEDGE_STATUS)
        Log.d(TAG, "DataWedge status: $dwStatus")
        ScanStateHolder.scanViewStatus.value = ScanViewState(
            dwStatus = DWStatus(dwStatus == "ENABLED", dwStatus ?: "")
        )
        ScanStateHolder.isDataWedgeReady.value = true
    }
}