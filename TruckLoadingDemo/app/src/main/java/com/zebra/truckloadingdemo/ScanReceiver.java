// Copyright (c) 2020 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import static android.content.Context.RECEIVER_EXPORTED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

public class ScanReceiver {
    private final static String TAG = "ScanReceiver";
    private final static boolean DEBUG = false;

    private Context mContext;
    private OnScanListener mListener;


    public ScanReceiver(Context context) {
        mContext = context;

        //receive scan data from android
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction("com.dwexample.ACTION");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(myBroadcastReceiver, filter, RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(myBroadcastReceiver, filter);
        }
    }

    /**
     * This receives the broadcasted intent from datawedge upon a scan
     */
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if ("com.dwexample.ACTION".equals(action)) {
                //  Received a barcode scan
                final String decodedData = intent.getStringExtra(
                        mContext.getResources().getString(R.string.datawedge_intent_key_data));
                if (mListener != null) {
                    if (DEBUG) Log.e(TAG, "Received Message: " + decodedData);
                    mListener.onScan(decodedData);
                }
            }
        }
    };

    /**
     * Set a listener for new scan data
     *
     * @param listener {@link OnScanListener} to receive a call-back for new scan data
     */
    public void setOnScanListener(OnScanListener listener) {
        mListener = listener;
    }

    /**
     * Interface for handling when new scan data has been received
     */
    public interface OnScanListener {
        /**
         * Call-back to handle a scanned barcode
         *
         * @param data String of the barcode
         */
        void onScan(String data);
    }
}
