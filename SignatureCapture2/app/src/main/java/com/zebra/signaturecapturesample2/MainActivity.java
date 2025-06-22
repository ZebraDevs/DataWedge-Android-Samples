package com.zebra.signaturecapturesample2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SignatureCapture";
    TextView txtStatus = null;
    LinearLayout linearLayout = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txtStatus);
        linearLayout = findViewById(R.id.ll_result);
        registerReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryProfileList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceivers();
    }

    private void queryProfileList(){
        Intent i = new Intent();
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.putExtra(IntentKeys.EXTRA_GET_PROFILES_LIST, "");
        sendBroadcast(i);
    }

    private void deleteProfile() {
        Intent intent = new Intent();
        intent.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        intent.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        intent.putExtra(IntentKeys.EXTRA_DELETE_PROFILE,IntentKeys.PROFILE_NAME);
        this.sendBroadcast(intent);
    }

    private void registerReceivers(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentKeys.RESULT_ACTION);
        intentFilter.addAction(IntentKeys.NOTIFICATION_ACTION);
        intentFilter.addAction(IntentKeys.INTENT_OUTPUT_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unRegisterReceivers(){
        unregisterReceiver(broadcastReceiver);
    }

    public void onCreateProfile(View view){
        createProfile();
    }

    public void onClickClearScannedData(View view)
    {
        linearLayout.removeAllViews();
    }

    public void onClickScan(View view) {
        Intent i = new Intent();
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.putExtra(IntentKeys.EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        this.sendBroadcast(i);
    }

    private void createProfile(){
        Bundle profileConfig = new Bundle();

        Bundle barcodeConfig = new Bundle();
        Bundle barcodeParams = new Bundle();
        ArrayList<Bundle> bPluginConfigList = new ArrayList<>();

        /*###Configuration for Barcode Input [start]###*/
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");//Plugin name as Barcode
        barcodeParams.putString("scanner_selection", "auto"); //Make scanner selection as auto
        barcodeParams.putString("decoder_signature","true"); //enable decode signature
        barcodeConfig.putString("RESET_CONFIG", "true"); //Reset existing configurations of barcode input plugin
        barcodeConfig.putBundle("PARAM_LIST", barcodeParams);
        bPluginConfigList.add(barcodeConfig);
        /*###Configuration for Barcode Input [Finish]###*/


        /*###Configuration for Intent Output[Start]###*/
        Bundle intentConfig = new Bundle();
        Bundle intentParams = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT"); //Plugin name as intent
        intentConfig.putString("RESET_CONFIG", "true"); //Reset existing configurations of intent output plugin
        intentParams.putString("intent_output_enabled", "true"); //Enable intent output plugin
        intentParams.putString("intent_action", IntentKeys.INTENT_OUTPUT_ACTION); //Set the intent action
        intentParams.putString("intent_category", IntentKeys.INTENT_CATEGORY); //Set a category for intent
        intentParams.putInt("intent_delivery", IntentKeys.INTENT_DELIVERY); // Set intent delivery mechanism
        intentParams.putString("intent_use_content_provider", "true"); //Enable content provider
        intentConfig.putBundle("PARAM_LIST", intentParams);
        bPluginConfigList.add(intentConfig);
        /*### Configurations for Intent Output[Finish] ###*/

        profileConfig.putParcelableArrayList("PLUGIN_CONFIG", bPluginConfigList);//Putting the INTENT and BARCODE plugin settings to the PLUGIN_CONFIG extra

        /*### Associate this application to the profile [Start] ###*/
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME",getPackageName());//Get Package name of the application
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});//Add all activities of this application
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{
                appConfig
        });
        /*### Associate this application to the profile [Finish] ###*/

        profileConfig.putString("PROFILE_NAME", IntentKeys.PROFILE_NAME); //Initialize the profile name
        profileConfig.putString("PROFILE_ENABLED","true");//Enable the profile
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");
        profileConfig.putString("RESET_CONFIG", "true");//Enable reset configuration if already exist

        Intent intent = new Intent();
        intent.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        intent.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        intent.putExtra(IntentKeys.DATAWEDGE_CONFIG, profileConfig);
        intent.putExtra("SEND_RESULT", "COMPLETE_RESULT");
        intent.putExtra(IntentKeys.COMMAND_IDENTIFIER_EXTRA,
                IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE);
        this.sendBroadcast(intent);

    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();

                if (intent.hasExtra(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST)){
                    String[] arrayProfileList = extras.getStringArray(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST);
                    List<String> profileList = Arrays.asList(arrayProfileList);
                    //check whether the profile is exist or not
                    if(profileList.contains(IntentKeys.PROFILE_NAME)){
                        //if the profile is already exist
                        setStatus("Profile already exists, not creating the profile");
                    }else{
                        //if the profile doest exist
                        setStatus("Profile does not exists. Creating the profile..");
                        createProfile();
                    }
                }
                else if(extras.containsKey(IntentKeys.COMMAND_IDENTIFIER_EXTRA)){
                    /*## Processing the result of CREATE_PROFILE[Start] ###*/
                    if(extras.getString(IntentKeys.COMMAND_IDENTIFIER_EXTRA)
                            .equalsIgnoreCase(IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE)){
                        ArrayList<Bundle> bundleList = intent.getParcelableArrayListExtra("RESULT_LIST");
                        if (bundleList != null && bundleList.size()>0){
                            boolean allSuccess = true;
                            StringBuilder resultInfo = new StringBuilder();
                            //Iterate through the result list for each module
                            for(Bundle bundle : bundleList){
                                if (bundle.getString("RESULT")
                                        .equalsIgnoreCase(IntentKeys.INTENT_RESULT_CODE_FAILURE)){
                                    //if the profile creation failure for that module, provide more information on that
                                    allSuccess = false;
                                    resultInfo.append("Module Name : ")
                                            .append(bundle.getString("MODULE"))
                                            .append("\n"); //Name of the module

                                    resultInfo.append("Result code: ").
                                            append(bundle.getString("RESULT_CODE")).
                                            append("\n");//Information of the moule

                                    if(bundle.containsKey("SUB_RESULT_CODE")) {
                                        resultInfo.append("\tSub Result code: ")
                                                .append(bundle.getString("SUB_RESULT_CODE"))
                                                .append("\n");
                                    }
                                    break; // Breaking the loop as there is a failure
                                }else {
                                    //if the profile creation success for the module.
                                    resultInfo.append("Module: " )
                                            .append(bundle.getString("MODULE"))
                                            .append("\n");

                                    resultInfo.append("Result: ")
                                            .append(bundle.getString("RESULT"))
                                            .append("\n");
                                }
                            }
                            if (allSuccess) {
                                setStatus("Profile created successfully");
                            } else {
                                setStatus("Profile creation failed!\n\n" + resultInfo);
                                deleteProfile();
                            }
                        }
                    }
                    /*### Processing the result of CREATE_PROFILE [Finish] ###*/
                }
                else if(action.equals(IntentKeys.INTENT_OUTPUT_ACTION)){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = intent.getExtras();
                            if (bundle != null){
                                String decodedMode = bundle.getString(IntentKeys.DECODED_MODE);
                                Log.e(TAG, "Decode Mode: "+ decodedMode);
                                processingDecodeData(bundle); //Processing the decode data
                            }
                        }
                    });
                    thread.start();
                }
            }catch (Exception ex){
                Log.e(TAG, "onReceive: ", ex);
            }
        }
    };


    private synchronized void processingDecodeData(Bundle data){
        /*## Processing decode the data[Start] ###*/
        String decodeDataUri = data.getString(IntentKeys.IMAGE_URI);
        //check the data if it is coming from content provider
        if(decodeDataUri != null){
            Cursor cursor = getContentResolver().query(Uri.parse(decodeDataUri),
                    null, data, null);
            if (cursor != null){
                cursor.moveToFirst();
                String barCodeData = "";
                String labelType = data.getString(IntentKeys.LABEL_TYPE_TAG);
                int imageFormat = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_FORMAT)));//Get image format 1 - JPEG, 3 - BMP,  4 - TIFF, 5 - YUV
                barCodeData += "\nLabel type: " + labelType;
                barCodeData += "\nSignature : " + cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_SIGNATURE_TYPE));
                barCodeData += "\nImage format : " + getImageFormat(imageFormat); //Getting image format
                barCodeData += "\nImage Size : " + cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_SIZE))+ " bytes";

                //Checking image format
                if(imageFormat != 4)
                    barCodeData += "\nImage data: ";//Checking if signature is present in the field [Finish]
                else
                    barCodeData += "\nImage data: The app captures images only in .JPG (default) and .BMP format";//Checking if signature is present in the field [Finish]

                TextView txtBarcodeData = new TextView(getApplicationContext());
                txtBarcodeData.setText(barCodeData);
                setUIForResult(txtBarcodeData, null);

                String nextURI = cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_NEXT_URI));
                byte[] binaryData = null;
                if (nextURI.isEmpty()) { //No data chunks. All data are available in one chunk
                    binaryData = cursor.getBlob(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_DATA));
                }else{
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        final String fullDataSize = cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_FULL_DATA_SIZE));
                        int bufferSize = cursor.getInt(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_BUFFER));
                        baos.write(cursor.getBlob(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_DATA))); //Read the first chunk from initial set
                        while (!nextURI.isEmpty()) {
                            Cursor imageDataCursor = getContentResolver().query(Uri.parse(nextURI), null, null, null);
                            if (imageDataCursor != null) {
                                imageDataCursor.moveToFirst();
                                bufferSize += imageDataCursor.getInt(imageDataCursor.getColumnIndexOrThrow(IntentKeys.IMAGE_BUFFER));
                                byte[] bufferData = imageDataCursor.getBlob(imageDataCursor.getColumnIndexOrThrow(IntentKeys.IMAGE_DATA));
                                baos.write(bufferData);
                                nextURI = imageDataCursor.getString(imageDataCursor.getColumnIndexOrThrow(IntentKeys.IMAGE_NEXT_URI));
                            }
                            assert imageDataCursor != null;
                            imageDataCursor.close();
                            final int finalBufferSize = bufferSize;
                            Log.d(TAG,"Data being processed, please wait..\n" + finalBufferSize + "/" + fullDataSize + " bytes merged");
                        }
                        binaryData = baos.toByteArray();
                        baos.close();
                    }
                    catch (final Exception ex)
                    {
                        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                try {
                    //-- Creating Bitmap Image [Start]
                    Bitmap bmp = null;
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    if (binaryData != null) {
                        bmp = BitmapFactory.decodeByteArray(binaryData, 0,
                                binaryData.length);
                        if(imageFormat != 4)//compress image if it is in .JPEG or BMP
                            bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    }
                    final ImageView img = new ImageView(getApplicationContext());
                    img.setImageBitmap(bmp);
                    setUIForResult(null, img);
                    //-- Creating Bitmap Image [Finish]
                } catch (final Exception ex) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
            setStatus("Data processing successful");
        }
        /*## Processing decode the data[End] ###*/
    }

    private String getImageFormat(int type){
        String imageFormat = "";
        switch (type){
            case 1:
                imageFormat = "JPEG";
                break;
            case 3:
                imageFormat = "BMP";
                break;
            case 4:
                imageFormat = "TIFF";
                break;
            case 5:
                imageFormat = "YUV";
                break;
        }
        return imageFormat;
    }

    public void setStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText("Status: " + status);
            }
        });
    }

    private void setUIForResult(TextView textView, ImageView imageView){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textView!=null){
                    linearLayout.addView(textView);
                }
                if(imageView != null){
                    linearLayout.addView(imageView);
                }
            }
        });
    }

}