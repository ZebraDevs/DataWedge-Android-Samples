package com.zebra.sample.multifragmentsample1.data

// Object defining constants for interacting with Zebra DataWedge APIs.
object DataWedgeConstant {

    // Synchronization lock for thread-safe DataWedge intent calls.
    val dwIntentCallLock = Any()

    // Custom event action for broadcasting intents to the application.
    const val APPLICATION_EVENT_ACTION = "com.zebra.sample.MultiFragmentSample1.ACTION"

    // Profile name used for configuring DataWedge profile.
    const val EXTRA_PROFILENAME = "MultiFragmentProfile"

    // Intent actions for DataWedge API operations.
    const val EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"
    const val EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE"
    const val EXTRA_SWITCH_SCANNER_PARAMS = "com.symbol.datawedge.api.SWITCH_SCANNER_PARAMS"
    const val EXTRA_NOTIFICATIONS = "com.symbol.datawedge.api.NOTIFICATION"
    const val EXTRA_ENUMERATE_SCANNERS = "com.symbol.datawedge.api.ENUMERATE_SCANNERS"
    const val EXTRA_REGISTER_FOR_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION"
    const val EXTRA_UNREGISTER_FOR_NOTIFICATION = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION"
    const val EXTRA_GET_DATAWEDGE_STATUS = "com.symbol.datawedge.api.GET_DATAWEDGE_STATUS"
    const val EXTRA_COMMAND_IDENTIFIER = "COMMAND_IDENTIFIER"

    // General actions for DataWedge communication.
    const val ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION"
    const val ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION"
    const val ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION"

    // Output constants for scanned data results.
    const val OUTPUT_DATA_STRING = "com.symbol.datawedge.data_string"
    const val OUTPUT_DATA_LABEL_TYPE = "com.symbol.datawedge.label_type"

    // Result actions for DataWedge operations.
    const val RESULT_GET_DATAWEDGE_STATUS = "com.symbol.datawedge.api.RESULT_GET_DATAWEDGE_STATUS"
    const val RESULT_RESULT_ENUMERATE_SCANNERS = "com.symbol.datawedge.api.RESULT_ENUMERATE_SCANNERS"

    // Command identifiers for specific operations.
    const val VALUE_COMMAND_IDENTIFIER_SCAN_PARAM = "COMMAND_IDENTIFIER_SCAN_PARAM"
    const val VALUE_COMMAND_IDENTIFIER_SET_CONFIG = "VALUE_COMMAND_IDENTIFIER_SET_CONFIG"
    const val VALUE_COMMAND_IDENTIFIER_CREATE_PROFILE = "COMMAND_IDENTIFIER_CREATE_PROFILE"
    const val VALUE_COMMAND_IDENTIFIER_SWITCH_SCANNER_EX = "COMMAND_IDENTIFIER_SWITCH_SCANNER_EX"

    // Notification identifiers for scanner and profile-related events.
    const val SCANNER_STATUS = "SCANNER_STATUS"
    const val PROFILE_SWITCH = "PROFILE_SWITCH"

    // Delay values for polling operations.
    const val DW_STATUS_POLLING_DELAY = 500L
    const val POLLING_WAIT_DELAY = 200L
}
