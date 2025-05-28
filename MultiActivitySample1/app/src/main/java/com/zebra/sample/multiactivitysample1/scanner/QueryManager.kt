package com.zebra.sample.multiactivitysample1.scanner

import android.content.Intent
import android.util.Log
import com.zebra.sample.multiactivitysample1.App
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.ACTION_DATAWEDGE
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_ENUMERATE_SCANNERS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.EXTRA_GET_DATAWEDGE_STATUS
import com.zebra.sample.multiactivitysample1.data.DataWedgeConstant.dwIntentCallLock

// Manager for sending broadcast queries to DataWedge to retrieve status and enumerate scanners.
class QueryManager {
    private val TAG = this.javaClass.canonicalName

    // Sends a broadcast intent to the DataWedge API in a thread-safe manner.
    private fun sendBroadcast(intent: Intent) {
        synchronized(dwIntentCallLock) {
            Log.d(TAG, "sendBroadcast [start]")
            App.getInstance().sendBroadcast(intent)
            Log.d(TAG, "sendBroadcast [end]")
        }
    }

    // Sends a broadcast to query the current status of DataWedge.
    fun getDataWedgeStatus() {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_GET_DATAWEDGE_STATUS, "")
        }
        sendBroadcast(intent)
    }

    // Sends a broadcast to request the enumeration of available scanners from DataWedge.
    fun enumerateScanners() {
        // Create intent to enumerate all available scanners.
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_ENUMERATE_SCANNERS, "")
        }
        // Send the broadcast to DataWedge.
        sendBroadcast(intent)
    }
}
