package com.zebra.sample.multiactivitysample1.scanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import com.zebra.sample.multiactivitysample1.App
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.ACTION_DATAWEDGE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.APPLICATION_EVENT_ACTION
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.COMMAND_IDENTIFIER
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_CREATE_PROFILE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_PROFILE_NAME_1
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_PROFILE_NAME_2
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_SET_CONFIG
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_SWITCH_SCANNER_EX
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_SWITCH_SCANNER_PARAMS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_SWITCH_TO_PROFILE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.REGISTER_FOR_NOTIFICATION
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.UNREGISTER_FOR_NOTIFICATION
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SET_CONFIG
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SCAN_PARAM
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SWITCH_PROFILE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.VALUE_COMMAND_IDENTIFIER_SWITCH_SCANNER_EX
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.dwIntentCallLock
import com.zebra.sample.multiactivitysample1.data.models.ActivityName
import com.zebra.sample.multiactivitysample1.data.models.Symbology

// Manager for handling DataWedge configurations and actions through broadcast intents.
class ConfigurationManager {
    private val TAG = this.javaClass.canonicalName

    // Sends a broadcast intent to the DataWedge API in a thread-safe manner.
    private fun sendBroadcast(intent: Intent) {
        synchronized(dwIntentCallLock) {
            Log.d(TAG, "sendBroadcast [start]")
            App.getInstance().sendBroadcast(intent)
            Log.d(TAG, "sendBroadcast [end]")
        }
    }

    // Creates a DataWedge profile based on the specified activity name.
    fun createProfile(activityName: ActivityName) {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(
                EXTRA_CREATE_PROFILE,
                if (activityName == ActivityName.FIRST) EXTRA_PROFILE_NAME_1 else EXTRA_PROFILE_NAME_2
            )
            putExtra("SEND_RESULT", "true")
            putExtra(COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE)
        }
        sendBroadcast(intent)
    }

    // Updates the configuration for profile 1 with specific symbology and plugin settings.
    fun updateProfile1() {
        Log.d(TAG, "updateProfile -> 1")

        // List of symbologies to enable in the barcode configuration.
        val symbologies = listOf(
            Symbology.CODE_39, Symbology.CODE_128, Symbology.QR_CODE, Symbology.UPCA, Symbology.UPCA
        )

        // Profile configuration bundle for updating DataWedge settings.
        val profileConfig = bundleOf(
            "PROFILE_NAME" to EXTRA_PROFILE_NAME_1,
            "PROFILE_ENABLED" to "true",
            "CONFIG_MODE" to "CREATE_IF_NOT_EXIST",
            "RESET_CONFIG" to "true",
            // Application array specifying which apps use this profile.
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

        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_SET_CONFIG, profileConfig)
            putExtra("SEND_RESULT", "true")
            putExtra(COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SET_CONFIG)
        }
        sendBroadcast(intent)
    }

    // Registers for specified notifications from DataWedge.
    fun registerForNotifications(list: List<String>) {
        list.forEach {
            val b = bundleOf(
                "com.symbol.datawedge.api.APPLICATION_NAME" to App.getInstance().packageName,
                "com.symbol.datawedge.api.NOTIFICATION_TYPE" to it
            )
            val intent = Intent(ACTION_DATAWEDGE).apply {
                putExtra(REGISTER_FOR_NOTIFICATION, b)
            }
            sendBroadcast(intent)
        }
    }

    // Unregisters from specified notifications from DataWedge.
    fun unregisterForNotifications(list: List<String>) {
        list.forEach {
            val b = bundleOf(
                "com.symbol.datawedge.api.APPLICATION_NAME" to App.getInstance().packageName,
                "com.symbol.datawedge.api.NOTIFICATION_TYPE" to it
            )
            val intent = Intent(ACTION_DATAWEDGE).apply {
                putExtra(UNREGISTER_FOR_NOTIFICATION, b)
            }
            sendBroadcast(intent)
        }
    }

    // Toggles the soft scan trigger using DataWedge.
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
            putExtra(COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SCAN_PARAM)
        }
        sendBroadcast(intent)
    }

    // Switches the active scanner to the specified scanner name.
    fun switchScanner(scanner_name: String) {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_SWITCH_SCANNER_EX, scanner_name)
            putExtra("SEND_RESULT", "true")
            putExtra(COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SWITCH_SCANNER_EX)
        }
        sendBroadcast(intent)
    }

    // Switches the current profile to the specified profile name.
    fun switchToProfile(profileName: String) {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_SWITCH_TO_PROFILE, profileName)
            putExtra("SEND_RESULT", "true")
            putExtra(COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER_SWITCH_PROFILE)
        }
        sendBroadcast(intent)
    }
}
