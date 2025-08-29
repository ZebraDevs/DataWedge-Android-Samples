/*
 * Copyright (C) 2018-2023 Zebra Technologies Corp
 * All rights reserved.
 */
package com.zebra.freeformocrsample1;

import static com.zebra.freeformocrsample1.IntentKeys.DATAWEDGE_API_ACTION;
import static com.zebra.freeformocrsample1.IntentKeys.INTENT_OUTPUT_ACTION;
import static com.zebra.freeformocrsample1.IntentKeys.PROFILE_NAME;
import static com.zebra.freeformocrsample1.IntentKeys.SELECTED_WORKFLOW;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView txtWorkflowStatus, txtProfileStatus = null;
    LinearLayout layoutRegion = null;
    Button btnClear;

    private final String TAG = MainActivity.class.getCanonicalName();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtWorkflowStatus = findViewById(R.id.txtStatus);
        txtProfileStatus = findViewById(R.id.txtProfileStatus);
        btnClear = findViewById(R.id.btnClear);
        layoutRegion = findViewById(R.id.layoutRegeions);

        registerReceivers();
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(IntentKeys.RESULT_ACTION);
        filter.addAction(IntentKeys.NOTIFICATION_ACTION);
        filter.addAction(IntentKeys.INTENT_OUTPUT_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        registerUnregisterForNotifications(false, IntentKeys.NOTIFICATION_TYPE_WORKFLOW_STATUS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryProfileList();
        registerUnregisterForNotifications(true, IntentKeys.NOTIFICATION_TYPE_WORKFLOW_STATUS);
    }

    void registerUnregisterForNotifications(boolean register, String type) {
        Bundle b = new Bundle();
        b.putString(IntentKeys.DATAWEDGE_API_NAME, getPackageName());
        b.putString(IntentKeys.NOTIFICATION_TYPE, type);
        Intent i = new Intent();
        i.putExtra("APPLICATION_PACKAGE", getPackageName());
        i.setAction(DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);

        if (register)
            i.putExtra(IntentKeys.REGISTER_NOTIFICATION, b);
        else
            i.putExtra(IntentKeys.UNREGISTER_NOTIFICATION, b);

        this.sendBroadcast(i);
    }

    void createProfile() {

        Bundle bMain = new Bundle();

        Bundle bConfigWorkflow = new Bundle();
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();

        /*###### Configurations for Workflow Input [Start] ######*/
        bConfigWorkflow.putString("PLUGIN_NAME", "WORKFLOW");
        bConfigWorkflow.putString("RESET_CONFIG", "true"); //Reset existing configurations of barcode input plugin

        bConfigWorkflow.putString("workflow_input_enabled", "true");
        bConfigWorkflow.putString("selected_workflow_name", SELECTED_WORKFLOW);
        bConfigWorkflow.putString("workflow_input_source", "1");


        bConfigWorkflow.putParcelableArrayList("PARAM_LIST", getWorkflowInputParams());
        bundlePluginConfig.add(bConfigWorkflow);
        /*###### Configurations for Workflow Input [Finish] ######*/

        /*###### Configurations for Intent Output [Start] ######*/
        Bundle bConfigIntent = new Bundle();
        Bundle bParamsIntent = new Bundle();
        bConfigIntent.putString("PLUGIN_NAME", "INTENT");
        bConfigIntent.putString("RESET_CONFIG", "true"); //Reset existing configurations of intent output plugin
        bParamsIntent.putString("intent_output_enabled", "true"); //Enable intent output plugin
        bParamsIntent.putString("intent_action", INTENT_OUTPUT_ACTION); //Set the intent action
        bParamsIntent.putString("intent_category", "android.intent.category.DEFAULT"); //Set a category for intent
        bParamsIntent.putInt("intent_delivery", 2); // Set intent delivery mechaism, Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast, "3" for start foreground service
        bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);
        bundlePluginConfig.add(bConfigIntent);
        /*###### Configurations for Intent Output [Finish] ######*/

        //Putting the INTENT and BARCODE plugin settings to the PLUGIN_CONFIG extra
        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);


        /*###### Associate this application to the profile [Start] ######*/
        Bundle configApplicationList = new Bundle();
        configApplicationList.putString("PACKAGE_NAME", getPackageName());
        configApplicationList.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        bMain.putParcelableArray("APP_LIST", new Bundle[]{
                configApplicationList
        });
        /* ###### Associate this application to the profile [Finish] ######*/

        bMain.putString("PROFILE_NAME", PROFILE_NAME); //Specify the profile name
        bMain.putString("PROFILE_ENABLED", "true"); //Enable the profile
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");
        bMain.putString("RESET_CONFIG", "true");

        Intent iSetConfig = new Intent();

        iSetConfig.setAction(DATAWEDGE_API_ACTION);
        iSetConfig.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        iSetConfig.putExtra(IntentKeys.SET_CONFIG, bMain);
        iSetConfig.putExtra("SEND_RESULT", "COMPLETE_RESULT");
        iSetConfig.putExtra(IntentKeys.COMMAND_IDENTIFIER_EXTRA,
                IntentKeys.COMMAND_IDENTIFIER_CREATE_PROFILE);
        this.sendBroadcast(iSetConfig);

    }

    private void deleteProfile() {
        Intent i = new Intent();
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.putExtra(IntentKeys.EXTRA_DELETE_PROFILE, IntentKeys.PROFILE_NAME);
        sendBroadcast(i);
    }

    private void queryProfileList() {
        Intent i = new Intent();
        i.setAction(IntentKeys.DATAWEDGE_API_ACTION);
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.putExtra(IntentKeys.EXTRA_GET_PROFILES_LIST, "");
        sendBroadcast(i);
    }

    public void btnOnClickCreateProfile(View view) {
        createProfile();
    }

    public void btnOnClickClearScannedData(View view) {
        layoutRegion.removeAllViews();
    }

    public void btnOnClickScan(View view) {
        Intent i = new Intent();
        i.setPackage(IntentKeys.DATAWEDGE_PACKAGE);
        i.setAction(DATAWEDGE_API_ACTION);
        i.putExtra(IntentKeys.EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        this.sendBroadcast(i);
    }

    private Bundle getWorkflowParamBundle(String workflowName) {
        Bundle paramList = new Bundle();
        paramList.putString("workflow_name", workflowName);
        paramList.putString("workflow_input_source", "1");

        Bundle paramSetFeedbackModule = new Bundle();
        paramSetFeedbackModule.putString("module", "BarcodeTrackerModule");
        Bundle moduleParamsFeedback = new Bundle();
        moduleParamsFeedback.putString("decode_and_highlight_barcodes", "1");
        paramSetFeedbackModule.putBundle("module_params", moduleParamsFeedback);

        ArrayList<Bundle> paramSetList = new ArrayList<>();
        paramSetList.add(paramSetFeedbackModule);

        paramList.putParcelableArrayList("workflow_params", paramSetList);
        return paramList;
    }

    private ArrayList<Bundle> getWorkflowInputParams() {
        ArrayList<Bundle> workFlowList = new ArrayList<>();
        workFlowList.add(getWorkflowParamBundle(SELECTED_WORKFLOW));
        return workFlowList;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("Range")
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // Check the profile list returned from the EXTRA_GET_PROFILES_LIST method
            /* ###### Processing quarried profile list [Start] ###### */
            if (intent.hasExtra(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST)) {

                String[] profilesList = extras.
                        getStringArray(IntentKeys.EXTRA_RESULT_GET_PROFILES_LIST);
                List<String> arrProfileList = Arrays.asList(profilesList);
                // Check if the profile list contains the DocumentCapture profile
                if (arrProfileList.contains(PROFILE_NAME)) {
                    updateProfileStatus("Profile already exists, not creating the profile");
                    // Profile exists, no need to create the profile again
                } else {
                    // Profile does not exist, create the profile
                    updateProfileStatus("Profile does not exists. Creating the profile..");
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
                            updateProfileStatus("Profile created successfully");
                        } else {

                            updateProfileStatus("Profile creation failed!\n\n" + resultInfo);
                            deleteProfile();
                        }
                    }
                }
            }
            /* ###### Processing the result of CREATE_PROFILE [End] ###### */

            /* ###### Processing the result of NOTIFICATION_ACTION [Start] ###### */
            else if (action.equals(IntentKeys.NOTIFICATION_ACTION)) {

                if (intent.hasExtra(IntentKeys.NOTIFICATION)) {
                    Bundle b = intent.getBundleExtra(IntentKeys.NOTIFICATION);
                    String NOTIFICATION_TYPE = b.getString("NOTIFICATION_TYPE");
                    if (NOTIFICATION_TYPE != null) {
                        switch (NOTIFICATION_TYPE) {
                            case IntentKeys.NOTIFICATION_TYPE_WORKFLOW_STATUS:
                                //Must register for scanner status notification
                                String status = b.getString("STATUS");
                                updateWorkflowStatus(status);
                                break;
                        }

                    }
                }
            }
            /* ###### Processing the result of NOTIFICATION_ACTION [End] ###### */

            /* ###### Processing the result of INTENT_OUTPUT [Start] ###### */
            else if (action.equalsIgnoreCase(INTENT_OUTPUT_ACTION)) {
                TextView txtBarcodeData = new TextView(getApplicationContext());
                Bundle bundle = intent.getExtras();
                String jsonData = bundle.getString(IntentKeys.DATA_TAG);
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.has(IntentKeys.STRING_DATA_KEY)) {
                            //String data
                            outputFreeFormOcrStringData(jsonArray, txtBarcodeData);
                        } else if (jsonObject.has("uri")) {
                            //Image data
                            String uri = jsonObject.getString("uri");
                            outputFreeFormOCRImageData(uri, jsonObject);
                        }

                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error to receive data: " + ex.getMessage());
                }

            }
            /* ###### Processing the result of INTENT_OUTPUT [End] ###### */
        }
    };


    private void outputFreeFormOcrStringData(JSONArray array, TextView txtBarcodeData) throws JSONException {

        String fullDataString = "";
        String prevBlock = null;
        String prevGroup = null;
        for (int i = 0; i < array.length(); i++) {
            try {
                if (!array.getJSONObject(i).has(IntentKeys.JSON_IMAGE_FORMAT)) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    String blockLabel = jsonObject.get(IntentKeys.KEY_BLOCK_LABEL).toString();
                    String groupLabel = jsonObject.get(IntentKeys.KEY_GROUP_ID).toString();
                    String stringData = jsonObject.get(IntentKeys.KEY_STRING_DATA).toString();

                    if (prevBlock != null && !prevBlock.equals(blockLabel))
                        fullDataString = fullDataString + "\n\n";

                    if (prevGroup != null && !prevGroup.equals(groupLabel))
                        fullDataString = fullDataString + "\n";

                    fullDataString = fullDataString + stringData + " ";

                    prevGroup = groupLabel;
                    prevBlock = blockLabel;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        txtBarcodeData.setText(fullDataString);
        showInUI(txtBarcodeData, null);

    }

    private synchronized void outputFreeFormOCRImageData(String uri, JSONObject jsonObject) throws IOException, JSONException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Cursor cursor = getContentResolver().query(Uri.parse(uri), null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                baos.write(cursor.getBlob(cursor.getColumnIndexOrThrow(IntentKeys.RAW_DATA)));
                String nextURI = cursor.getString(cursor.getColumnIndexOrThrow(IntentKeys.DATA_NEXT_URI));

                while (nextURI != null && !nextURI.isEmpty()) {
                    try (Cursor cursorNextData = getContentResolver().query(Uri.parse(nextURI), null, null, null)) {
                        if (cursorNextData != null && cursorNextData.moveToFirst()) {
                            baos.write(cursorNextData.getBlob(cursorNextData.getColumnIndexOrThrow(IntentKeys.RAW_DATA)));
                            nextURI = cursorNextData.getString(cursorNextData.getColumnIndexOrThrow(IntentKeys.DATA_NEXT_URI));
                        }
                    }

                }
            }
        }

        int width = 0;
        int height = 0;
        int stride = 0;
        int orientation = 0;
        String imageFormat = "";

        width = jsonObject.getInt(IntentKeys.IMAGE_WIDTH);
        height = jsonObject.getInt(IntentKeys.IMAGE_HEIGHT);
        stride = jsonObject.getInt(IntentKeys.STRIDE);
        orientation = jsonObject.getInt(IntentKeys.ORIENTATION);
        imageFormat = jsonObject.getString(IntentKeys.IMAGE_FORMAT);

        Bitmap bitmap = ImageProcessing.getInstance().getBitmap(baos.toByteArray(), imageFormat, orientation, stride, width, height);

        final ImageView img = new ImageView(getApplicationContext());
        img.setImageBitmap(bitmap);
        showInUI(null, img);
    }

    private void showInUI(final TextView textView, final ImageView imageView) {
        runOnUiThread(() -> {
            if (textView != null)
                layoutRegion.addView(textView);

            if (imageView != null)
                layoutRegion.addView(imageView);
        });
    }

    void updateWorkflowStatus(final String status) {
        runOnUiThread(() -> txtWorkflowStatus.setText("Workflow Status: " + status));
    }

    void updateProfileStatus(final String status) {
        runOnUiThread(() -> txtProfileStatus.setText("Profile Status: " + status));
    }
}