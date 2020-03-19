package com.zebra.profileswitch1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private static final String DATAWEDGE_SWITCH_TO_PROFILE = "com.symbol.datawedge.api.SWITCH_TO_PROFILE";
    private static final String DATAWEDGE_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String DATAWEDGE_CATEGORY = "android.intent.category.DEFAULT";

    private static final String DATAWEDGE_SEND_RESULT = "SEND_RESULT";
    private static final String DATAWEDGE_EXTRA_COMMAND = "COMMAND";
    private static final String DATAWEDGE_EXTRA_RESULT = "RESULT";
    private static final String DATAWEDGE_EXTRA_RESULT_INFO = "RESULT_INFO";
    private static final String DATAWEDGE_EXTRA_RESULT_SUCCESS = "SUCCESS";
    private static final String PROFILE_NAME = "PROFILE_NAME";

    // DataWedge Actions
    private static final String DATAWEDGE_ACTION = "com.symbol.datawedge.api.ACTION";
    private static final String DATAWEDGE_ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION";

    private static final String PROFILE_NAME_1 = "switch_profile1";
    private static final String PROFILE_NAME_2 = "switch_profile2";
    private static final String PROFILE_NAME_3 = "switch_profile3";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText field1 = findViewById(R.id.editText1);
        field1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    switchToProfile(PROFILE_NAME_1);
                }
            }
        });

        EditText field2 = findViewById(R.id.editText2);
        field2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    switchToProfile(PROFILE_NAME_2);
                }
            }
        });

        EditText field3 = findViewById(R.id.editText3);
        field3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    switchToProfile(PROFILE_NAME_3);
                }
            }
        });

        // Create test profiles to demonstrate switching
        sendDataWedgeCommand(DATAWEDGE_CREATE_PROFILE, PROFILE_NAME_1);
        sendDataWedgeCommand(DATAWEDGE_CREATE_PROFILE, PROFILE_NAME_2);
        sendDataWedgeCommand(DATAWEDGE_CREATE_PROFILE, PROFILE_NAME_3);
    }

    void switchToProfile(String profileName){
        Intent dwIntent = new Intent();
        dwIntent.setAction(DATAWEDGE_ACTION);
        dwIntent.putExtra(DATAWEDGE_SWITCH_TO_PROFILE, profileName);
        dwIntent.putExtra(DATAWEDGE_SEND_RESULT,"true");
        this.sendBroadcast(dwIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register broadcast receiver to listen for responses from DW API
        IntentFilter dwIntentFilter = new IntentFilter();
        dwIntentFilter.addAction(DATAWEDGE_ACTION_RESULT);
        dwIntentFilter.addCategory(DATAWEDGE_CATEGORY);
        registerReceiver(myBroadcastReceiver, dwIntentFilter);
    }

    void sendDataWedgeCommand(String action, String parameter) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(DATAWEDGE_ACTION);
        dwIntent.putExtra(action, parameter);
        this.sendBroadcast(dwIntent);
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(DATAWEDGE_EXTRA_COMMAND)) {
                if(intent.getExtras().getString(DATAWEDGE_EXTRA_COMMAND).equals(DATAWEDGE_SWITCH_TO_PROFILE)) {

                    Bundle bundle = intent.getExtras();
                    if(bundle.getString(DATAWEDGE_EXTRA_RESULT).equalsIgnoreCase(DATAWEDGE_EXTRA_RESULT_SUCCESS))
                    {
                        Bundle resultInfoBundle = bundle.getBundle(DATAWEDGE_EXTRA_RESULT_INFO);
                        handleActiveProfileChanged(resultInfoBundle.getString(PROFILE_NAME));
                    }

                }
            }
        }
    };

    private void handleActiveProfileChanged (String profileName)
    {
        TextView activeProfileText = findViewById(R.id.txtActiveProfile);
        activeProfileText.setText(profileName);
    }

}
