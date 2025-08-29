
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

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DW_ACTION_SigCap = "com.zebra.signaturecapture.action.SigCap";
    public static final String DW_CATEGORY_SigCap = "com.zebra.signaturecapture.category.SigCap";
    public static final String PROFILE_NAME_SIG_CAP = "SignatureCapture";

    ImageView ImgV;
    int height;
    int width;
    File imagesDirectory = null;
    TextView informationBox;

    @Override
    protected void onResume() {
        super.onResume();

        requestPermissionStorage();
        Intent i = getIntent();
        if (i != null)
            handleIntent(i);
    }

    final int REQUEST_READ_STORAGE = 12345;

    private void requestPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }
    }

    File getImagesSaveDirectory() {
        File file = new File(this.getExternalMediaDirs()[0] + "/SigCap/");
        if (!file.exists() && file.mkdir()) {
            Log.d(TAG, "Directory successfully created");
        } else {
            Log.i(TAG, "Directory exists");
        }
        return file;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(DW_ACTION_SigCap);
        filter1.addCategory(DW_CATEGORY_SigCap);

        myBroadcastReceiver = new MyBroadcastReceiver();
        myBroadcastReceiver.handler = new Handler();
        myBroadcastReceiver.setActivity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, filter1, RECEIVER_EXPORTED);
        } else {
            registerReceiver(myBroadcastReceiver, filter1);
        }

        Window w = this.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        height = lp.height;
        width = lp.width;
        informationBox = findViewById(R.id.textPath);
    }

    MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    public void clearImageOnly() {
        if (ImgV != null) {
            ImgV.setImageDrawable(null);
        }
    }

    /**
     * Display the image in the UI and save it in the app's media imagesDirectory
     *
     * @param imageUriArray - Uri list returned from DataWedge
     * @param ext           - image format/extension e.g: jpg
     */
    public void showImage(ArrayList<Uri> imageUriArray, String ext) {
        Log.d(TAG, "showImage(..)");
        try {
            if (imageUriArray != null) {
                if (imagesDirectory == null) {
                    imagesDirectory = getImagesSaveDirectory();
                }
                int i = 0;

                for (Uri dwImgUri : imageUriArray) {

                    int w = 0;
                    int h = 0;
                    ImgV = findViewById(R.id.imageView1);
                    Bitmap myBitmap;
                    InputStream is;

                    try {

                        is = getContentResolver().openInputStream(dwImgUri);
                        if (is != null) {

                            // Handle jpeg images
                            if (ext.equalsIgnoreCase("jpg")) {
                                myBitmap = BitmapFactory.decodeStream(is);
                                if (myBitmap != null) {
                                    ImgV.setImageDrawable(null);
                                    Drawable d;
                                    w = myBitmap.getWidth();
                                    h = myBitmap.getHeight();
                                    String s = "Width:" + w + ",Height:" + h;

                                    Log.d(TAG, s);
                                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(myBitmap, w, h, true));
                                    ImgV.setImageDrawable(d);

                                    i++;
                                    File imageFPath = writeJpegToFile(imagesDirectory, i, ext, myBitmap);
                                    String message = imageFPath != null ? imageFPath.getAbsolutePath() : "Not successful!";
                                    Log.d(TAG, "result from writeJpegToFile(): " + message);  // CW

                                    putInformationalMessage("Saved jpeg image file:" + message);

                                    is.close();
                                }
                            }
                            // Handle bmp images
                            if (ext.equalsIgnoreCase("bmp")) {
                                myBitmap = BitmapFactory.decodeStream(is);
                                if (myBitmap != null) {
                                    ImgV.setImageDrawable(null);
                                    Drawable d;
                                    w = myBitmap.getWidth();
                                    h = myBitmap.getHeight();
                                    String s = "Width:" + w + ",Height:" + h;

                                    Log.d(TAG, s);
                                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(myBitmap, w, h, true));
                                    ImgV.setImageDrawable(d);

                                    i++;
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                    String fileStorePath = imagesDirectory.getAbsolutePath() + "/SigCap" + "_" + w + "X" + h + "_" + timeStamp + "_" + i + "." + ext;
                                    new BmpUtility().save(myBitmap, fileStorePath);
                                    Log.d(TAG, "Bmp path: " + fileStorePath);

                                    putInformationalMessage("Saved bmp image file:" + fileStorePath);

                                    is.close();
                                }
                            }

                            // Handle tiff image - NOT SUPPORTED IN THIS SAMPLE
                            if (ext != null && ext.equalsIgnoreCase("tiff")) {

                                // Add your code here to support .tiff images
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Error occurred:" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "showImage : " + e.getMessage());
        }
    }

    File writeJpegToFile(File directory, int id, String extension, Bitmap bitmap) {
        File file = null;

        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileStorePath = directory.getAbsolutePath() + "/SigCap" + "_" + width + "X" + height + "_" + timeStamp + "_" + id + "." + extension;
            file = new File(fileStorePath);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
                if (extension.equalsIgnoreCase("jpg"))
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "writeJpegToFile()" + e.getMessage());
            }
        }
        return file;
    }

    // Use DataWedge API SetConfig : http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
    public void setConfiguration(View view) {

        // Main bundle properties
        Bundle bundleMain = new Bundle();
        // profile name and state
        bundleMain.putString("PROFILE_NAME", PROFILE_NAME_SIG_CAP);
        bundleMain.putString("PROFILE_ENABLED", "true");
        // Create DataWedge profile if it does not exist
        bundleMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        // Associate profile with this app
        Bundle bundleApp1 = new Bundle();
        bundleApp1.putString("PACKAGE_NAME", getPackageName());
        bundleApp1.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        // Add app list bundle into the main bundle
        bundleMain.putParcelableArray("APP_LIST", new Bundle[]{
                bundleApp1 //other entries
        });

        // Configure barcode params
        Bundle bundleBarcodeParams = new Bundle();
        bundleBarcodeParams.putString("scanner_input_enabled", "true");
        bundleBarcodeParams.putString("decoder_signature", "true");
        EditText w = findViewById(R.id.width);
        EditText h = findViewById(R.id.height);
        bundleBarcodeParams.putString("decoder_signature_height", h != null ? h.getText().toString() : "100");
        bundleBarcodeParams.putString("decoder_signature_width", w != null ? w.getText().toString() : "400");
        bundleBarcodeParams.putString("decoder_signature_bpp", "2");    //(0 - 1BPP, 1 - 4BPP, 2- 8BPP)
        bundleBarcodeParams.putString("decoder_signature_format", "1"); //(1 – JPEG, 3 – BMP,  4 – TIFF )
        bundleBarcodeParams.putString("decoder_signature_jpegquality", "95");   //(5 – 100, multiples of 5)

        // auto or valid scanner identifier:
        bundleBarcodeParams.putString("scanner_selection_by_identifier", "AUTO");

        // Plugin config bundle properties
        Bundle bConfigBarcode = new Bundle();
        bConfigBarcode.putString("PLUGIN_NAME", "BARCODE");
        bConfigBarcode.putString("RESET_CONFIG", "false");
        bConfigBarcode.putBundle("PARAM_LIST", bundleBarcodeParams);

        // Configure intent output for captured data to be sent to this app
        Bundle bundleIntentOutConfig = new Bundle();
        bundleIntentOutConfig.putString("PLUGIN_NAME", "INTENT");
        bundleIntentOutConfig.putString("RESET_CONFIG", "false");

        // Param list properties
        Bundle bundleIntentParams = new Bundle();
        bundleIntentParams.putString("intent_output_enabled", "true");
        bundleIntentParams.putString("intent_action", DW_ACTION_SigCap);
        bundleIntentParams.putString("intent_category", DW_CATEGORY_SigCap);
        bundleIntentParams.putString("intent_delivery", "2");   //0 - activity, 1 - service, 2 - Broadcast
        bundleIntentOutConfig.putBundle("PARAM_LIST", bundleIntentParams);

        // Disable keystroke output plugin
        Bundle bundleKSOutConfig = new Bundle();
        bundleKSOutConfig.putString("PLUGIN_NAME", "KEYSTROKE");
        bundleKSOutConfig.putString("RESET_CONFIG", "false");
        Bundle bundleKSParams = new Bundle();
        bundleKSParams.putString("keystroke_output_enabled", "false");
        bundleKSOutConfig.putBundle("PARAM_LIST", bundleKSParams);

        // Add each bundle to the profile configuration
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();
        bundlePluginConfig.add(bConfigBarcode);
        bundlePluginConfig.add(bundleIntentOutConfig);
        bundlePluginConfig.add(bundleKSOutConfig);

        bundleMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);

        Intent bundleSetConfig = new Intent();
        bundleSetConfig.setAction("com.symbol.datawedge.api.ACTION");
        bundleSetConfig.putExtra("com.symbol.datawedge.api.SET_CONFIG", bundleMain);
        bundleSetConfig.putExtra("SEND_RESULT", "COMPLETE_RESULT"); //Supported values: NONE, LAST_RESULT, COMPLETE_RESULT
        bundleSetConfig.putExtra("COMMAND_IDENTIFIER", "INTENT_API");

        Log.d(TAG, "Calling Set_Config API");
        this.sendBroadcast(bundleSetConfig);
        Toast.makeText(this, PROFILE_NAME_SIG_CAP + " Profile and Parameters updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewIntent(Intent i) {
        final Intent intent = i;
        handleIntent(intent);
    }

    public void putInformationalMessage(String message) {
        if (informationBox != null) {
            informationBox.setText(message);
        }
    }

    void handleIntent(Intent i) {
        if (i == null)
            return;
        myBroadcastReceiver.handleIncomingImageUri(i);
    }

}
