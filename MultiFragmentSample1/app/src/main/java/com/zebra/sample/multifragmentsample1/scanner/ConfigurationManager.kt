package com.zebra.sample.multifragmentsample1.scanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import com.zebra.sample.multifragmentsample1.App
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.ACTION_DATAWEDGE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_COMMAND_IDENTIFIER
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.APPLICATION_EVENT_ACTION
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_CREATE_PROFILE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_PROFILENAME
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_SET_CONFIG
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_SWITCH_SCANNER_PARAMS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_REGISTER_FOR_NOTIFICATION
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_UNREGISTER_FOR_NOTIFICATION
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SET_CONFIG
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SCAN_PARAM
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.dwIntentCallLock
import com.zebra.sample.multifragmentsample1.data.models.Symbology

class ConfigurationManager {

    private val TAG = this.javaClass.canonicalName

    // Sends a broadcast intent to DataWedge API in a thread-safe manner using a lock.
    private fun sendBroadcast(intent: Intent) {
        synchronized(dwIntentCallLock) {
            Log.d(TAG, "sendBroadcast [start]")
            App.getInstance().sendBroadcast(intent)
            Log.d(TAG, "sendBroadcast [end]")
        }
    }

    // Creates a new profile in DataWedge.
    fun createProfile() {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_CREATE_PROFILE, EXTRA_PROFILENAME) // Profile name to create.
            putExtra("SEND_RESULT", "true") // Request a result for the operation.
            putExtra(EXTRA_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE) // Unique command identifier.
        }
        sendBroadcast(intent)
    }

    // Updates the profile configuration with new settings.
    fun updateProfile() {
        Log.d(TAG, "createProfile()")

        // List of symbologies to enable in the barcode configuration.
        val symbologies = listOf(
            Symbology.CODE_39, Symbology.CODE_128, Symbology.QR_CODE, Symbology.UPCA, Symbology.UPCA
        )

        // Profile configuration bundle for updating DataWedge settings.
        val profileConfig = bundleOf(
            "PROFILE_NAME" to EXTRA_PROFILENAME,
            "PROFILE_ENABLED" to "true",
            "CONFIG_MODE" to "CREATE_IF_NOT_EXIST",
            "RESET_CONFIG" to "true",
            // Application list specifying which apps use this profile.
            "APP_LIST" to arrayOf(
                bundleOf(
                    "PACKAGE_NAME" to App.getInstance().packageName,
                    "ACTIVITY_LIST" to arrayOf("*"),
                ),
            ),
            // Plugin configuration for barcode, intent, and keystroke plugins.
            "PLUGIN_CONFIG" to listOf(
                bundleOf(
                    "PLUGIN_NAME" to "BARCODE",
                    "RESET_CONFIG" to "true",
                    "PARAM_LIST" to bundleOf(
                        "scanner_selection" to "auto",
                        "scanner_input_enabled" to "true",
                        "configure_all_scanners" to "true",
                        "decoder_code128" to symbologies.contains(Symbology.CODE_128).toString(),
                        "decoder_code39" to symbologies.contains(Symbology.CODE_39).toString(),
                        "decoder_ean13" to symbologies.contains(Symbology.EAN_13).toString(),
                        "decoder_upca" to symbologies.contains(Symbology.UPCA).toString(),
                        "decoder_qrcode" to symbologies.contains(Symbology.QR_CODE).toString(),
                    ),
                ),
                bundleOf(
                    "PLUGIN_NAME" to "INTENT",
                    "RESET_CONFIG" to "true",
                    "PARAM_LIST" to bundleOf(
                        "intent_output_enabled" to "true",
                        "intent_action" to APPLICATION_EVENT_ACTION,
                        "intent_delivery" to 2,
                    ),
                ),
                bundleOf(
                    "PLUGIN_NAME" to "KEYSTROKE",
                    "RESET_CONFIG" to "true",
                    "PARAM_LIST" to bundleOf(
                        "keystroke_output_enabled" to "false",
                    ),
                ),
            ),
        )

        // Create and send the broadcast intent for updating the profile configuration.
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_SET_CONFIG, profileConfig)
            putExtra("SEND_RESULT", "true")
            putExtra(EXTRA_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SET_CONFIG)
        }
        sendBroadcast(intent)
    }

    // Registers for DataWedge notifications for the specified types.
    fun registerForNotifications(list: List<String>) {
        list.forEach {
            val b = bundleOf(
                "com.symbol.datawedge.api.APPLICATION_NAME" to App.getInstance().packageName,
                "com.symbol.datawedge.api.NOTIFICATION_TYPE" to it
            )
            val intent = Intent(ACTION_DATAWEDGE).apply {
                putExtra(EXTRA_REGISTER_FOR_NOTIFICATION, b)
            }
            sendBroadcast(intent)
        }
    }

    // Unregisters from DataWedge notifications for the specified types.
    fun unregisterForNotifications(list: List<String>) {
        list.forEach {
            val b = bundleOf(
                "com.symbol.datawedge.api.APPLICATION_NAME" to App.getInstance().packageName,
                "com.symbol.datawedge.api.NOTIFICATION_TYPE" to it
            )
            val intent = Intent(ACTION_DATAWEDGE).apply {
                putExtra(EXTRA_UNREGISTER_FOR_NOTIFICATION, b)
            }
            sendBroadcast(intent)
        }
    }

    // Toggles the scanning functionality using DataWedge's soft scan trigger.
    fun softScanToggle() {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "TOGGLE_SCANNING")
        }
        sendBroadcast(intent)
    }

    // Enables or disables the scanner input plugin in DataWedge.
    fun toggleScannerPlugin(isEnable: Boolean) {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(
                "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN",
                if (isEnable) "ENABLE_PLUGIN" else "DISABLE_PLUGIN"
            )
        }
        sendBroadcast(intent)
    }

    // Switches scanner parameters by sending a configuration bundle to DataWedge.
    fun switchScanParams(params: Bundle) {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_SWITCH_SCANNER_PARAMS, params)
            putExtra("SEND_RESULT", "true")
            putExtra(EXTRA_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SCAN_PARAM)
        }
        sendBroadcast(intent)
    }

    // Switches the active scanner by updating the profile configuration.
    fun switchScanner(scannerId: String) {
        val profileConfig = bundleOf(
            "PROFILE_NAME" to EXTRA_PROFILENAME,
            "PROFILE_ENABLED" to "true",
            "CONFIG_MODE" to "UPDATE",
            "PLUGIN_CONFIG" to listOf(
                bundleOf(
                    "PLUGIN_NAME" to "BARCODE",
                    "PARAM_LIST" to bundleOf(
                        "scanner_selection" to scannerId,
                        "scanner_input_enabled" to "true",
                    ),
                ),
            ),
        )

        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_SET_CONFIG, profileConfig)
            putExtra("SEND_RESULT", "true")
            putExtra(EXTRA_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SET_CONFIG)
        }
        sendBroadcast(intent)
    }
}
