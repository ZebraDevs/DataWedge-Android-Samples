package com.zebra.documentcapturesample1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * Template "SignatureAndAddressSeparately" must be present in device prior to running this sample.
 * Flow of the sample:
 * 1. Register broadcast receiver.
 * 2. Query if DocumentCaptureProfile profile exists (result will be received in the broadcast receiver).
 * 3. If profile does not exist, create the profile with NextGen SimulScan configurations.
 *      3.1. Set scanning mode as "Document Capture".
 *      3.2. Configure Intent Output action, category and delivery mechanism. Enable content provider.
 *      3.3. Associate this application to the profile.
 * 4. If profile creation failed for one or more plugins, delete the profile.
 * 5. Clicking on "Toggle Scanning" button will start/stop scanning.
 * 6. On a successful decode, decoded data will be received by to the broadcast receiver with the action given in the Intent Output.
 *
 */

public class MainActivity extends AppCompatActivity {

    public static String TEMPLATE_NAME = "SignatureAndAddressSeparately";
    TextView txtStatus = null;
    TextView txtTemplate = null;
    LinearLayout layoutRegeions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txtStatus);
        txtTemplate  = findViewById(R.id.txtTemplate);
        txtTemplate.setText("Template \"" + TEMPLATE_NAME + "\", must be available in the device to run this application. Please use DataWedgeMGR via StageNow to push the templates to device");
        layoutRegeions = findViewById(R.id.layoutRegeions);
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

    private void queryProfileList()
    {
        Intent i = new Intent();
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.putExtra(IntentKeys.EXTRA_GET_PROFILES_LIST, "");
        sendBroadcast(i);
    }

    private void deleteProfile()
    {
        Intent i = new Intent();
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.putExtra(IntentKeys.EXTRA_DELETE_PROFILE, IntentKeys.PROFILE_NAME);
        sendBroadcast(i);
    }

    void createProfile() {

        Bundle bMain = new Bundle();

        Bundle bConfigBarcode = new Bundle();
        Bundle bParamsBarcode = new Bundle();
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();

        /*###### Configurations for Barcode Input [Start] ######*/
        bConfigBarcode.putString("PLUGIN_NAME", "BARCODE");
        bParamsBarcode.putString("scanner_selection", "auto"); // Make scanner selection as auto
        bParamsBarcode.putString("scanning_mode", String.valueOf(IntentKeys.DOCUMENT_CAPTURE_SCANNING_MODE)); // Set the scanning mode as "Document Capture"
        //bParamsBarcode.putString("illumination_mode", "off"); // Turn off Illumination to scan a document from a reflective screen
        bParamsBarcode.putString("doc_capture_template", TEMPLATE_NAME); // Give a template name
        bConfigBarcode.putString("RESET_CONFIG", "true"); // Reset existing configurations of barcode input plugin
        bConfigBarcode.putBundle("PARAM_LIST", bParamsBarcode);
        bundlePluginConfig.add(bConfigBarcode);
        /*###### Configurations for Barcode Input [Finish] ######*/

        /*###### Configurations for Intent Output [Start] ######*/
        Bundle bConfigIntent = new Bundle();
        Bundle bParamsIntent = new Bundle();
        bConfigIntent.putString("PLUGIN_NAME", "INTENT");
        bConfigIntent.putString("RESET_CONFIG", "true"); // Reset existing configurations of intent output plugin
        bParamsIntent.putString("intent_output_enabled", "true"); // Enable intent output plugin
        bParamsIntent.putString("intent_action", IntentKeys.INTENT_OUTPUT_ACTION); // Set the intent action
        bParamsIntent.putString("intent_category", "android.intent.category.DEFAULT"); // Set a category for intent
        bParamsIntent.putInt("intent_delivery", 2); // Set intent delivery mechanism, Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast, "3" for start foreground service
        bParamsIntent.putString("intent_use_content_provider", "true"); // Enable content provider
        bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);
        bundlePluginConfig.add(bConfigIntent);
        /*###### Configurations for Intent Output [Finish] ######*/

        //Putting the INTENT and BARCODE plugin settings to the PLUGIN_CONFIG extra
        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);


        /*###### Associate this application to the profile [Start] ######*/
        Bundle configApplicationList = new Bundle();
        configApplicationList.putString("PACKAGE_NAME",getPackageName());
        configApplicationList.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        bMain.putParcelableArray("APP_LIST", new Bundle[]{
                configApplicationList
        });
        /* ###### Associate this application to the profile [Finish] ######*/

        bMain.putString("PROFILE_NAME", IntentKeys.PROFILE_NAME); //Specify the profile name
        bMain.putString("PROFILE_ENABLED", "true"); // Enable the profile
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");
        bMain.putString("RESET_CONFIG", "true");

        Intent iSetConfig = new Intent();

        iSetConfig.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        iSetConfig.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        iSetConfig.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
        iSetConfig.putExtra("SEND_RESULT", "COMPLETE_RESULT");
        iSetConfig.putExtra(IntentKeys.COMMAND_IDENTIFIER_EXTRA,
                IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE);

        this.sendBroadcast(iSetConfig);
    }

    private void registerReceivers() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(IntentKeys.RESULT_ACTION);
        filter.addAction(IntentKeys.NOTIFICATION_ACTION);
        filter.addAction(IntentKeys.INTENT_OUTPUT_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, filter);
    }

    void unRegisterReceivers() {
        unregisterReceiver(myBroadcastReceiver);
    }

    public void btnOnClickCreateProfile(View view)
    {
        createProfile();
    }

    public void btnOnClickClearScannedData(View view)
    {
        layoutRegeions.removeAllViews();
    }

    public void btnOnClickScan(View view)
    {
        Intent i = new Intent();
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.putExtra(IntentKeys.EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        this.sendBroadcast(i);
    }

    void updateStatus(final String status)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText("Status: " + status);
            }
        });
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            try {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();

                // Check the profile list returned from the EXTRA_GET_PROFILES_LIST method
                /* ###### Processing quarried profile list [Start] ###### */
                if (intent.hasExtra(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST)) {

                    String[] profilesList = extras.
                            getStringArray(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST);
                    List<String> arrProfileList = Arrays.asList(profilesList);
                    // Check if the profile list contains the DocumentCapture profile
                    if (arrProfileList.contains(IntentKeys.PROFILE_NAME)) {
                        updateStatus("Profile already exists, not creating the profile");
                        // Profile exists, no need to create the profile again
                    } else {
                        // Profile does not exist, create the profile
                        updateStatus("Profile does not exists. Creating the profile..");
                        createProfile();
                    }

                }
                /* ###### Processing queried profile list [Finish] ###### */

                /* ###### Processing the result of CREATE_PROFILE [Start] ###### */
                else if (extras.containsKey(IntentKeys.COMMAND_IDENTIFIER_EXTRA)) {

                    // Check if the create profile command succeeded for
                    // Barcode Input and Intent Output modules
                    if (extras.getString(IntentKeys.COMMAND_IDENTIFIER_EXTRA)
                            .equalsIgnoreCase(IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE)) {
                        ArrayList<Bundle> result_list = (ArrayList<Bundle>) extras.get("RESULT_LIST");
                        if (result_list != null && result_list.size() > 0) {
                            boolean allSuccess = true;
                            String resultInfo = "";
                            // Iterate through the result list for each module
                            for (Bundle result : result_list) {
                                if (result.getString("RESULT")
                                        .equalsIgnoreCase(IntentKeys.INTENT_RESULT_CODE_FAILURE)) {

                                    // Profile creation failed for the module.
                                    // Getting more information on what failed
                                    allSuccess = false;
                                    resultInfo = "Module: " + result
                                            .getString("MODULE") + "\n"; // Name of the module that failed
                                    resultInfo += "Result code: " + result
                                            .getString("RESULT_CODE") + "\n"; // Information on the type of the failure
                                    if (result.containsKey("SUB_RESULT_CODE")) // More Information on the failure if exists
                                        resultInfo += "\tSub Result code: " + result
                                                .getString("SUB_RESULT_CODE") + "\n";

                                    break; // Breaking the loop as there is a failure
                                } else {
                                    // Profile creation success for the module.
                                    resultInfo = "Module: " + result.getString("MODULE") + "\n";
                                    resultInfo += "Result: " + result.getString("RESULT") + "\n";
                                }
                            }

                            if (allSuccess) {
                                updateStatus("Profile created successfully");
                            } else {

                                updateStatus("Profile creation failed!\n\n" + resultInfo);
                                deleteProfile();
                            }
                        }
                    }
                }
                /* ###### Processing the result of CREATE_PROFILE [Finish] ###### */

                /* ###### Processing scanned data from Intent output [Start] ###### */
                else if (action.equals(IntentKeys.INTENT_OUTPUT_ACTION)) {

                    Thread dataProcessingThrad = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle data = intent.getExtras();
                            if (data != null) {

                                String decodedMode = data.getString(IntentKeys.DECODED_MODE);

                                /* ###### Processing scanned data when ScanningMode is set as "Single" [Start] ###### */
                                if (decodedMode.equals(IntentKeys.SINGLE_DECODE_MODE)) {
                                    processSingleDecode(data);
                                }
                                /* ###### Processing scanned data when ScanningMode is set as "Single" [Finish] ###### */

                                /* ###### Processing scanned data when ScanningMode is set as "SimulScan" [Start] ###### */
                                else if (decodedMode.equals(IntentKeys.MULTIPLE_DECODE_MODE)) {
                                    processMultipleDecode(data);
                                }
                                /* ###### Processing scanned data when ScanningMode is set as "SimulScan" [Finish] ###### */
                            }
                        }
                    });
                    dataProcessingThrad.start();

                }
                /* ###### Processing scanned data from Intent output [Finish] ###### */

            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void processSingleDecode(Bundle data)
    {
        String decodeDataUri = data.getString(IntentKeys.DECODE_DATA_EXTRA);
        String barcodeData = "";
        // Check if the data is coming through the content provider.
        if(decodeDataUri != null) {
            // Data is coming through the content provider, using a Cursor object to extract data
            Cursor cursor = getContentResolver()
                    .query(Uri.parse(decodeDataUri), null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                String labelType = cursor
                        .getString(cursor.getColumnIndexOrThrow(IntentKeys.LABEL_TYPE));
                String dataString = cursor
                        .getString(cursor.getColumnIndexOrThrow(IntentKeys.STRING_DATA_KEY_SINGLE_BARCODE));

                barcodeData += "\nLabel type: " + labelType;
                barcodeData += "\nString data: " + dataString;
            }
        }
        else
        {
            // Data is coming through the Intent bundle itself
            String labelType = data.getString(IntentKeys.LABEL_TYPE_TAG);
            String dataString = data.getString(IntentKeys.STRING_DATA_KEY);

            barcodeData += "\nLabel type: " + labelType;
            barcodeData += "\nString data: " + dataString;
        }

        TextView txtBarcodeData = new TextView(getApplicationContext());
        txtBarcodeData.setText(barcodeData);

        showInUI(txtBarcodeData, null);
        updateStatus("Data processing successful");
    }

    private synchronized void processMultipleDecode(Bundle data)
    {
        ArrayList<Bundle> fields = data.getParcelableArrayList(IntentKeys.DATA_TAG);
        if(fields == null) // Content provider is not enabled in Intent Output plugin or Scanning mode is not selected as "SimulScan"
        {
            updateStatus("Content provider is not enabled in Intent Output plugin " +
                    "or Scanning mode is not selected as \"SimulScan\".\nPlease check and try again");
            return;
        }

        try {
            // Iterate through each field
            for (Bundle field : fields) {

                String decodeDataUri = field.getString(IntentKeys.FIELD_DATA_URI);
                Cursor cursor = null;
                if (decodeDataUri != null)
                    cursor = getContentResolver().query(Uri.parse(decodeDataUri),
                            null, null, null);
                if (cursor != null) {
                    int imgWidth = 0;
                    int imgHeight = 0;
                    cursor.moveToFirst();

                    String strResultStatusData = "";
                    String labelType = "";

                    try {
                        labelType = cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.FIELD_LABEL_TYPE));
                    } catch (Exception ex) {
                    }

                    strResultStatusData += "\nLabel type: " + labelType;
                    if (labelType.equals(IntentKeys.LABEL_TYPE_SIGNATURE)) {
                        imgWidth = cursor.getInt(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_WIDTH_TAG));
                        imgHeight = cursor.getInt(cursor.getColumnIndexOrThrow(IntentKeys.IMAGE_HEIGHT_TAG));
                        // Checking if signature is present in the field [Start]
                        try {
                            int signature_status = -2;
                            signature_status = cursor.getInt(cursor.getColumnIndexOrThrow(IntentKeys.COLUMN_SIGNATURE_STATUS));
                            if (signature_status == 1) {
                                //Signature present
                                strResultStatusData += "\nSignature status: Signature is present";
                            } else if (signature_status == 0) {
                                //Signature not present
                                strResultStatusData += "\nSignature status: Signature is not present";
                            } else if (signature_status == -1) {
                                //Signature not requested
                                strResultStatusData += "\nSignature status: Signature check is not requested";
                            } else if (signature_status == -2) {
                                //Signature not requested
                                strResultStatusData += "\nSignature status: Signature check is not supported";
                            }
                        } catch (Exception ex) {
                            strResultStatusData += "\nSignature status: Signature check is not supported";
                        }
                        //Checking if signature is present in the field [Finish]
                        strResultStatusData += "\nImage data: ";
                    } else {
                        String dataString = cursor
                                .getString(cursor.getColumnIndexOrThrow(IntentKeys.DATA_STRING));
                        strResultStatusData += "\nString data: " + dataString;
                    }

                    String nextURI = cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.DATA_NEXT_URI));
                    byte[] binaryData = null;
                    if (nextURI.isEmpty()) { // No data chunks. All data are available in one chunk
                        binaryData = cursor.getBlob(cursor.getColumnIndexOrThrow(IntentKeys.DECODE_DATA));
                    } else {
                        try {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            final String fullDataSize = cursor
                                    .getString(cursor.getColumnIndexOrThrow(IntentKeys.FULL_DATA_SIZE));
                            int bufferSize = cursor.getInt(cursor
                                    .getColumnIndexOrThrow(IntentKeys.RAW_DATA_SIZE));
                            baos.write(cursor.getBlob(cursor
                                    .getColumnIndexOrThrow(IntentKeys.DECODE_DATA))); // Read the first chunk from initial set
                            while (!nextURI.isEmpty()) {
                                Cursor imageDataCursor = getContentResolver()
                                        .query(Uri.parse(nextURI), null,
                                                null, null);
                                if (imageDataCursor != null) {
                                    imageDataCursor.moveToFirst();
                                    bufferSize += imageDataCursor
                                            .getInt(imageDataCursor
                                                    .getColumnIndexOrThrow(IntentKeys.RAW_DATA_SIZE));
                                    byte[] bufferData = imageDataCursor
                                            .getBlob(imageDataCursor
                                                    .getColumnIndexOrThrow(IntentKeys.DECODE_DATA));
                                    baos.write(bufferData);
                                    nextURI = imageDataCursor
                                            .getString(imageDataCursor
                                                    .getColumnIndexOrThrow(IntentKeys.DATA_NEXT_URI));
                                }
                                imageDataCursor.close();

                                updateStatus("Data being processed, please wait..\n" +
                                        bufferSize + "/" + fullDataSize + " bytes merged");
                            }
                            binaryData = baos.toByteArray();
                            baos.close();
                        } catch (final Exception ex) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, ex.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    final TextView txtBarcodeData = new TextView(getApplicationContext());
                    txtBarcodeData.setText(strResultStatusData);

                    showInUI(txtBarcodeData, null);

                    if (labelType.equals(IntentKeys.LABEL_TYPE_SIGNATURE)) {

                        try {
                            //-- Creating YUV Image and Bitmap Image [Start]
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            YuvImage yuvImage = new YuvImage(binaryData, ImageFormat.NV21,
                                    imgWidth, imgHeight, null);
                            yuvImage.compressToJpeg(new Rect(0, 0, imgWidth, imgHeight),
                                    50, out);
                            byte[] imageBytes = out.toByteArray();


                            Bitmap bmp = null;
                            if (binaryData != null) {
                                bmp = BitmapFactory.decodeByteArray(imageBytes, 0,
                                        imageBytes.length);
                            }
                            final ImageView img = new ImageView(getApplicationContext());
                            img.setImageBitmap(bmp);
                            showInUI(null, img);
                            //-- Creating YUV Image and Bitmap Image [Finish]
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
                }
            }
            updateStatus("Data processing successful");
        }
        catch (Exception ex)
        {
            //Log any errors
        }
    }

    private void showInUI(final TextView textView, final ImageView imageView)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(textView != null)
                    layoutRegeions.addView(textView);

                if(imageView != null)
                    layoutRegeions.addView(imageView);
            }
        });
    }
}
