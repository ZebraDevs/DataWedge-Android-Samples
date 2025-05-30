package com.zebra.sample.multifragmentsample1.scanner

import android.content.Intent
import android.util.Log
import com.zebra.sample.multifragmentsample1.App
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.ACTION_DATAWEDGE
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_ENUMERATE_SCANNERS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.EXTRA_GET_DATAWEDGE_STATUS
import com.zebra.sample.multifragmentsample1.data.DataWedgeConstant.dwIntentCallLock

class QueryManager {
    private val TAG = this.javaClass.canonicalName

    // Sends a broadcast intent to DataWedge API in a thread-safe manner using a synchronization lock.
    private fun sendBroadcast(intent: Intent) {
        synchronized(dwIntentCallLock) {
            Log.d(TAG, "sendBroadcast [start]")
            App.getInstance().sendBroadcast(intent)
            Log.d(TAG, "sendBroadcast [end]")
        }
    }

    // Queries the current status of DataWedge by sending a broadcast intent.
    fun getDataWedgeStatus() {
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_GET_DATAWEDGE_STATUS, "")
        }
        sendBroadcast(intent)
    }

    // Requests the enumeration of available scanners via DataWedge.
    fun enumerateScanners() {
        // Creates an intent to enumerate all available scanners.
        val intent = Intent(ACTION_DATAWEDGE).apply {
            putExtra(EXTRA_ENUMERATE_SCANNERS, "")
        }
        // Sends the broadcast to DataWedge.
        sendBroadcast(intent)
    }
}
