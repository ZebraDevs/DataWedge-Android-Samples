/*
 * Copyright (C) 2019 Zebra Technologies Corporation and/or its affiliates.
 * All rights reserved.
 *
 ***************************************************************************************
 *                                                                                      *
 *  This application is intended for demonstration purposes only. It is provided as-is  *
 *  without guarantee or warranty and may be modified to suit individual needs.         *
 *                                                                                      *
 *  Refer to Zebra's EULA: https://github.com/Zebra/samples-datawedge                   *
 *                                                                                      *
 ****************************************************************************************/


package com.zebra.signaturecapture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = MyBroadcastReceiver.class.getSimpleName();
    public Handler handler;
    MainActivity sigCapDisplayActivity;
    public static final String  BARCODE_DATA_FILE_PATHS = "com.symbol.datawedge.image_data_file_paths";
    public static final String 	BARCODE_IMAGE_FORMAT_TAG  =  "com.symbol.datawedge.image_format";
    public static final String 	BARCODE_SIGNATURE_TYPE_TAG  =  "com.symbol.datawedge.signature_type";
    public static final String 	BARCODE_IMAGE_SIZE_TAG  =  "com.symbol.datawedge.image_size";
    public static final String 	BARCODE_DATA_STRING_TAG  =  "com.symbol.datawedge.data_string";

    @Override
    public void onReceive(Context context, Intent intent) {
        handleIncomingImageUri(intent);
    }

    String getExtensionFromFormat(String format){
        String ext = "jpg";//default
        //(1 – JPEG, 3 – BMP,  4 – TIFF )
        if(format!=null){
            if("3".equals(format)){
                ext = "bmp";
            }else if("4".equals(format)){
                ext = "tiff";
            }
        }
        return ext;
    }

    public void handleIncomingImageUri(Intent intent) {
        Log.d(TAG, "In handleIncomingImageUri()");
        Bundle data = intent.getExtras();
        if (data != null) {
            final ArrayList<Uri> imageUris = data.getParcelableArrayList(BARCODE_DATA_FILE_PATHS);
            String format = data.getString(BARCODE_IMAGE_FORMAT_TAG);
            String sigType = data.getString(BARCODE_SIGNATURE_TYPE_TAG);
            String size = data.getString(BARCODE_IMAGE_SIZE_TAG);
            if (imageUris != null) {
                //Update UI to reflect multiple images being shared - currently not supported
                final String format2 = getExtensionFromFormat(format);
                Log.d(TAG, BARCODE_IMAGE_FORMAT_TAG + ":" + format);
                Log.d(TAG, BARCODE_SIGNATURE_TYPE_TAG + ":" + sigType);
                Log.d(TAG, BARCODE_IMAGE_SIZE_TAG + ":" + size);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        sigCapDisplayActivity.showImage(imageUris, format2);
                    }
                };

                handler.post(r);
                Log.d(TAG,"Posted to message Handler");
            }else{
                sigCapDisplayActivity.clearImageOnly();
                String barcodeData = data.getString(BARCODE_DATA_STRING_TAG);
                if(barcodeData!=null && !barcodeData.isEmpty()){
                    sigCapDisplayActivity.putInformationalMessage("Data:"+barcodeData);
                }else{
                    sigCapDisplayActivity.putInformationalMessage("No data");
                }
            }
        }
    }


    public void setActivity(MainActivity activity) {
        Log.d(TAG, "In setActivity()");
        sigCapDisplayActivity = activity;
    }
}
